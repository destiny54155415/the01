#pragma once

#include <stdint.h>

namespace neb {

enum class DrivingMode : uint8_t {
  Eco = 0,
  Comfort = 1,
  Sport = 2,
  Night = 3,
};

enum class NebulizerMode : uint8_t {
  Off = 0,
  Manual = 1,
  Auto = 2,  // AI spray regulation active
};

enum class VoiceState : uint8_t {
  Idle = 0,
  WakeHeard = 1,
  AwaitingCommand = 2,
};

enum class VoiceCommand : uint8_t {
  None = 0,
  Start,
  Stop,
  ModeAuto,
  ModeManual,
  LevelUp,
  LevelDown,
  LevelSet1,
  LevelSet2,
  LevelSet3,
  LevelSet4,
  LevelSet5,
  Status,
};

struct SensorSnapshot {
  float tank_pct;       // 0–100
  float humidity_pct;   // RH %
  float temperature_c;
  uint16_t aqi;         // stub air-quality index
  bool cartridge_present;
  bool valid;
};

struct SprayProfile {
  uint8_t base_level;     // 1–5
  float humidity_gain;    // multiply when cabin dry
  float aqi_gain;         // multiply when air quality poor
  float night_scale;      // applied in Night driving mode
  bool allow_auto_start;
};

struct DeviceState {
  bool running;
  NebulizerMode mode;
  uint8_t level;          // commanded / effective 0–5
  uint8_t effective_level;
  DrivingMode driving;
  SensorSnapshot sensors;
  VoiceState voice;
  bool dry_lock;
  bool mesh_cooling;
};

}  // namespace neb
