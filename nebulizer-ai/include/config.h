#pragma once

#include <stdint.h>

/** Firmware identity and tunable limits for the nebulizer AI stack. */

#ifndef NEBULIZER_FW_VERSION
#define NEBULIZER_FW_VERSION "1.0.0"
#endif

namespace neb {

/** Mist level 0 = off, 1–5 = duty table index. */
constexpr uint8_t kLevelMin = 1;
constexpr uint8_t kLevelMax = 5;
constexpr uint8_t kLevelDefault = 2;

/** Mesh / piezo burst duty (on_ms, off_ms). On-time capped for mesh life. */
constexpr uint16_t kMaxOnMs = 2500;
constexpr uint16_t kMeshCooldownMs = 15000;

struct DutyRow {
  uint16_t on_ms;
  uint16_t off_ms;
};

constexpr DutyRow kDutyTable[6] = {
    {0, 0},        // level 0 unused
    {400, 9000},   // 1
    {700, 7000},   // 2
    {1100, 5000},  // 3
    {1600, 4000},  // 4
    {2200, 3200},  // 5
};

/** Voice keyword FSM timing. */
constexpr uint32_t kWakeWindowMs = 4000;
constexpr uint32_t kCommandGapMs = 350;

/** Spray auto-regulation sample period. */
constexpr uint32_t kSprayControlPeriodMs = 500;

/** Tank: ADC raw mapped 0–100% (stub scale for demo). */
constexpr uint8_t kTankEmptyPct = 5;
constexpr uint8_t kTankLowPct = 20;

/** Cabin humidity / AQI thresholds used by spray regulation (stubs). */
constexpr float kHumidityLowPct = 30.0f;
constexpr float kHumidityHighPct = 65.0f;
constexpr uint16_t kAqiPoor = 100;

/** Serial demo console baud (ESP32). */
constexpr uint32_t kSerialBaud = 115200;

}  // namespace neb
