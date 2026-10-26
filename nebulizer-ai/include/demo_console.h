#pragma once

#include "driving_mode.h"
#include "sensors.h"
#include "spray_control.h"
#include "types.h"
#include "voice.h"

#include <stddef.h>
#include <stdint.h>

namespace neb {

/**
 * Serial / stdin command console for hardware-free demo.
 * Fake voice keywords, driving mode, and sensors.
 */
class DemoConsole {
 public:
  void begin(VoiceRecognizer* voice, DrivingModeController* drive, SensorHub* sensors,
             SprayController* spray, DeviceState* state);

  void poll();
  void print_help() const;
  void print_status() const;

 private:
  VoiceRecognizer* voice_ = nullptr;
  DrivingModeController* drive_ = nullptr;
  SensorHub* sensors_ = nullptr;
  SprayController* spray_ = nullptr;
  DeviceState* state_ = nullptr;

  char line_[96];
  size_t len_ = 0;

  void handle_line(const char* line);
};

}  // namespace neb
