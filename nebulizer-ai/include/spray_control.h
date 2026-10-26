#pragma once

#include "types.h"

#include <stdint.h>

namespace neb {

/**
 * Closed-loop / rule-based mist amount regulation.
 * Inputs: tank level, humidity, AQI stubs, driving-mode profile.
 */
class SprayController {
 public:
  void begin();

  void set_running(bool on);
  void set_mode(NebulizerMode mode);
  void set_manual_level(uint8_t level);
  void nudge_level(int delta);

  void tick(uint32_t now_ms, const SensorSnapshot& sensors, const SprayProfile& profile);

  bool running() const { return running_; }
  NebulizerMode mode() const { return mode_; }
  uint8_t commanded_level() const { return commanded_level_; }
  uint8_t effective_level() const { return effective_level_; }
  bool dry_lock() const { return dry_lock_; }
  bool mesh_cooling() const { return mesh_cooling_; }

 private:
  bool running_ = false;
  NebulizerMode mode_ = NebulizerMode::Auto;
  uint8_t commanded_level_ = 2;
  uint8_t effective_level_ = 0;
  bool dry_lock_ = false;
  bool mesh_cooling_ = false;

  uint32_t burst_phase_ms_ = 0;
  bool burst_on_ = false;
  uint32_t last_tick_ms_ = 0;
  uint32_t cooldown_until_ms_ = 0;
  uint32_t on_accum_ms_ = 0;

  uint8_t regulate(const SensorSnapshot& s, const SprayProfile& p) const;
  void drive_actuator(uint32_t now_ms, uint8_t level);
  void stop_actuator();
};

}  // namespace neb
