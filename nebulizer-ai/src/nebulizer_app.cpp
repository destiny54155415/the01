#include "nebulizer_app.h"

#include "board_hal.h"
#include "config.h"

namespace neb {

void NebulizerApp::begin() {
  hal::init();
  hal::console_begin(kSerialBaud);
  hal::console_printf("Nebulizer AI firmware %s\n", NEBULIZER_FW_VERSION);

  voice_.begin();
  drive_.begin();
  sensors_.begin();
  spray_.begin();
  spray_.set_mode(NebulizerMode::Auto);

  console_.begin(&voice_, &drive_, &sensors_, &spray_, &state_);
  sync_state();
}

void NebulizerApp::apply_voice_command(VoiceCommand cmd) {
  switch (cmd) {
    case VoiceCommand::None:
      break;
    case VoiceCommand::Start:
      spray_.set_running(true);
      break;
    case VoiceCommand::Stop:
      spray_.set_running(false);
      break;
    case VoiceCommand::ModeAuto:
      spray_.set_mode(NebulizerMode::Auto);
      break;
    case VoiceCommand::ModeManual:
      spray_.set_mode(NebulizerMode::Manual);
      break;
    case VoiceCommand::LevelUp:
      spray_.set_mode(NebulizerMode::Manual);
      spray_.nudge_level(+1);
      break;
    case VoiceCommand::LevelDown:
      spray_.set_mode(NebulizerMode::Manual);
      spray_.nudge_level(-1);
      break;
    case VoiceCommand::LevelSet1:
    case VoiceCommand::LevelSet2:
    case VoiceCommand::LevelSet3:
    case VoiceCommand::LevelSet4:
    case VoiceCommand::LevelSet5: {
      const uint8_t lvl = static_cast<uint8_t>(cmd) - static_cast<uint8_t>(VoiceCommand::LevelSet1) + 1;
      spray_.set_mode(NebulizerMode::Manual);
      spray_.set_manual_level(lvl);
      spray_.set_running(true);
      break;
    }
    case VoiceCommand::Status:
      console_.print_status();
      break;
  }
}

void NebulizerApp::on_driving_mode_changed() {
  const SprayProfile p = drive_.profile();
  /* Driving mode auto-change: adopt profile base level; keep Auto unless Night forces soft */
  if (spray_.mode() == NebulizerMode::Auto || spray_.mode() == NebulizerMode::Manual) {
    spray_.set_manual_level(p.base_level);
  }
  if (drive_.mode() == DrivingMode::Night && spray_.running()) {
    /* 야간: 자동 모드로 낮춰 조용히 */
    spray_.set_mode(NebulizerMode::Auto);
  }
  if (drive_.mode() == DrivingMode::Eco) {
    spray_.set_mode(NebulizerMode::Auto);
  }
  if (drive_.mode() == DrivingMode::Sport && p.allow_auto_start) {
    spray_.set_mode(NebulizerMode::Auto);
    spray_.set_running(true);
  }
  hal::console_printf("driving_mode -> %s (base_level=%u)\n",
                      DrivingModeController::mode_name(drive_.mode()), p.base_level);
}

void NebulizerApp::sync_state() {
  state_.running = spray_.running();
  state_.mode = spray_.mode();
  state_.level = spray_.commanded_level();
  state_.effective_level = spray_.effective_level();
  state_.driving = drive_.mode();
  state_.sensors = sensors_.read();
  state_.voice = voice_.state();
  state_.dry_lock = spray_.dry_lock();
  state_.mesh_cooling = spray_.mesh_cooling();
}

void NebulizerApp::loop() {
  const uint32_t now = hal::millis();

#if defined(NEBULIZER_SIM_CONSOLE)
  console_.poll();
#endif

  voice_.tick(now);
  const VoiceCommand vcmd = voice_.take_command();
  if (vcmd != VoiceCommand::None) {
    apply_voice_command(vcmd);
  }

  if (drive_.consume_changed()) {
    on_driving_mode_changed();
  }

  const SensorSnapshot sensors = sensors_.read();
  const SprayProfile profile = drive_.profile();
  spray_.tick(now, sensors, profile);

  sync_state();

  /* Slow status heartbeat in demo */
  static uint32_t last_hb = 0;
  if (now - last_hb > 5000) {
    last_hb = now;
    hal::console_printf("[hb] run=%d eff=%u drive=%s rh=%.0f tank=%.0f\n", state_.running ? 1 : 0,
                        state_.effective_level, DrivingModeController::mode_name(state_.driving),
                        static_cast<double>(state_.sensors.humidity_pct),
                        static_cast<double>(state_.sensors.tank_pct));
  }

  hal::delay_ms(10);
}

}  // namespace neb
