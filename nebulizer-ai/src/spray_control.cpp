#include "spray_control.h"

#include "board_hal.h"
#include "config.h"

namespace neb {
namespace {

uint8_t clamp_level(int v) {
  if (v < 0) return 0;
  if (v > static_cast<int>(kLevelMax)) return kLevelMax;
  return static_cast<uint8_t>(v);
}

}  // namespace

void SprayController::begin() {
  running_ = false;
  mode_ = NebulizerMode::Auto;
  commanded_level_ = kLevelDefault;
  effective_level_ = 0;
  dry_lock_ = false;
  mesh_cooling_ = false;
  burst_phase_ms_ = 0;
  burst_on_ = false;
  last_tick_ms_ = 0;
  cooldown_until_ms_ = 0;
  on_accum_ms_ = 0;
  stop_actuator();
}

void SprayController::set_running(bool on) {
  if (dry_lock_ && on) return;
  running_ = on;
  if (!on) {
    stop_actuator();
    effective_level_ = 0;
  }
}

void SprayController::set_mode(NebulizerMode mode) { mode_ = mode; }

void SprayController::set_manual_level(uint8_t level) {
  commanded_level_ = clamp_level(level == 0 ? 0 : static_cast<int>(level));
  if (commanded_level_ == 0) commanded_level_ = kLevelMin;
}

void SprayController::nudge_level(int delta) {
  set_manual_level(clamp_level(static_cast<int>(commanded_level_) + delta));
}

uint8_t SprayController::regulate(const SensorSnapshot& s, const SprayProfile& p) const {
  float level = static_cast<float>(p.base_level);

  if (s.humidity_pct < kHumidityLowPct) {
    level *= p.humidity_gain;
  } else if (s.humidity_pct > kHumidityHighPct) {
    level *= 0.7f;
  }

  if (s.aqi >= kAqiPoor) {
    level *= p.aqi_gain;
  }

  level *= p.night_scale;

  if (s.tank_pct < static_cast<float>(kTankLowPct)) {
    level *= 0.6f;
  }

  int out = static_cast<int>(level + 0.5f);
  if (out < static_cast<int>(kLevelMin)) out = kLevelMin;
  if (out > static_cast<int>(kLevelMax)) out = kLevelMax;
  return static_cast<uint8_t>(out);
}

void SprayController::stop_actuator() {
  hal::set_spray_pwm(0);
  hal::set_run_led(false);
  burst_on_ = false;
  burst_phase_ms_ = 0;
}

void SprayController::drive_actuator(uint32_t now_ms, uint8_t level) {
  if (level == 0 || level > kLevelMax) {
    stop_actuator();
    return;
  }

  if (now_ms < cooldown_until_ms_) {
    mesh_cooling_ = true;
    stop_actuator();
    return;
  }
  mesh_cooling_ = false;

  const DutyRow& duty = kDutyTable[level];
  if (last_tick_ms_ == 0) last_tick_ms_ = now_ms;
  const uint32_t dt = now_ms - last_tick_ms_;
  last_tick_ms_ = now_ms;
  burst_phase_ms_ += dt;

  const uint32_t period = static_cast<uint32_t>(duty.on_ms) + duty.off_ms;
  if (period == 0) {
    stop_actuator();
    return;
  }

  if (burst_phase_ms_ >= period) {
    burst_phase_ms_ %= period;
  }

  const bool want_on = burst_phase_ms_ < duty.on_ms;
  if (want_on) {
    /* Duty as PWM amplitude proxy — burst gated by on/off window */
    const uint8_t pwm = static_cast<uint8_t>(40 + level * 35);
    hal::set_spray_pwm(pwm);
    hal::set_run_led(true);
    on_accum_ms_ += dt;
    if (on_accum_ms_ > kMaxOnMs) {
      cooldown_until_ms_ = now_ms + kMeshCooldownMs;
      on_accum_ms_ = 0;
      mesh_cooling_ = true;
      stop_actuator();
      return;
    }
  } else {
    hal::set_spray_pwm(0);
    hal::set_run_led(false);
    on_accum_ms_ = 0;
  }
  burst_on_ = want_on;
}

void SprayController::tick(uint32_t now_ms, const SensorSnapshot& sensors, const SprayProfile& profile) {
  if (!sensors.cartridge_present) {
    dry_lock_ = false;
    set_running(false);
    stop_actuator();
    effective_level_ = 0;
    return;
  }

  if (sensors.tank_pct <= static_cast<float>(kTankEmptyPct)) {
    dry_lock_ = true;
    set_running(false);
    stop_actuator();
    effective_level_ = 0;
    hal::set_status_led((now_ms / 250) % 2 == 0);
    return;
  }
  dry_lock_ = false;
  hal::set_status_led(running_);

  if (mode_ == NebulizerMode::Auto) {
    effective_level_ = regulate(sensors, profile);
    if (!running_ && profile.allow_auto_start && sensors.humidity_pct < kHumidityLowPct) {
      running_ = true;
    }
    if (running_ && sensors.humidity_pct > kHumidityHighPct && profile.base_level <= 2) {
      /* Comfort/Eco: ease off when cabin already humid */
    }
  } else if (mode_ == NebulizerMode::Manual) {
    effective_level_ = commanded_level_;
  } else {
    effective_level_ = 0;
    running_ = false;
  }

  if (!running_ || mode_ == NebulizerMode::Off) {
    stop_actuator();
    if (!running_) effective_level_ = 0;
    return;
  }

  drive_actuator(now_ms, effective_level_);
}

}  // namespace neb
