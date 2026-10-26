#pragma once

#include "demo_console.h"
#include "driving_mode.h"
#include "sensors.h"
#include "spray_control.h"
#include "types.h"
#include "voice.h"

#include <stdint.h>

namespace neb {

class NebulizerApp {
 public:
  void begin();
  void loop();

  const DeviceState& state() const { return state_; }

 private:
  VoiceRecognizer voice_;
  DrivingModeController drive_;
  SensorHub sensors_;
  SprayController spray_;
  DemoConsole console_;
  DeviceState state_{};

  void apply_voice_command(VoiceCommand cmd);
  void sync_state();
  void on_driving_mode_changed();
};

}  // namespace neb
