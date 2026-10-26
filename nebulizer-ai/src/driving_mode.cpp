#include "driving_mode.h"

#include "config.h"

#include <ctype.h>
#include <string.h>

namespace neb {
namespace {

bool streq_ci(const char* a, const char* b) {
  if (!a || !b) return false;
  while (*a && *b) {
    if (tolower(static_cast<unsigned char>(*a)) != tolower(static_cast<unsigned char>(*b))) {
      return false;
    }
    ++a;
    ++b;
  }
  return *a == *b;
}

}  // namespace

void DrivingModeController::begin() {
  mode_ = DrivingMode::Comfort;
  changed_ = true;
}

void DrivingModeController::set_mode(DrivingMode mode) {
  if (mode_ == mode) return;
  mode_ = mode;
  changed_ = true;
}

bool DrivingModeController::consume_changed() {
  const bool c = changed_;
  changed_ = false;
  return c;
}

const char* DrivingModeController::mode_name(DrivingMode m) {
  switch (m) {
    case DrivingMode::Eco:
      return "eco";
    case DrivingMode::Comfort:
      return "comfort";
    case DrivingMode::Sport:
      return "sport";
    case DrivingMode::Night:
      return "night";
  }
  return "unknown";
}

bool DrivingModeController::parse_name(const char* name, DrivingMode* out) {
  if (!name || !out) return false;
  if (streq_ci(name, "eco") || streq_ci(name, "에코")) {
    *out = DrivingMode::Eco;
    return true;
  }
  if (streq_ci(name, "comfort") || streq_ci(name, "컴포트") || streq_ci(name, "normal")) {
    *out = DrivingMode::Comfort;
    return true;
  }
  if (streq_ci(name, "sport") || streq_ci(name, "스포츠")) {
    *out = DrivingMode::Sport;
    return true;
  }
  if (streq_ci(name, "night") || streq_ci(name, "나이트") || streq_ci(name, "야간")) {
    *out = DrivingMode::Night;
    return true;
  }
  return false;
}

SprayProfile DrivingModeController::profile() const {
  SprayProfile p{};
  switch (mode_) {
    case DrivingMode::Eco:
      /* 절약: 낮은 기본 단, 자동 시작 억제 */
      p.base_level = 1;
      p.humidity_gain = 1.1f;
      p.aqi_gain = 1.0f;
      p.night_scale = 1.0f;
      p.allow_auto_start = false;
      break;
    case DrivingMode::Comfort:
      p.base_level = 2;
      p.humidity_gain = 1.25f;
      p.aqi_gain = 1.15f;
      p.night_scale = 1.0f;
      p.allow_auto_start = true;
      break;
    case DrivingMode::Sport:
      /* 스포츠: 강한 분사, 공기질 반응↑ */
      p.base_level = 4;
      p.humidity_gain = 1.2f;
      p.aqi_gain = 1.35f;
      p.night_scale = 1.0f;
      p.allow_auto_start = true;
      break;
    case DrivingMode::Night:
      /* 야간: 조용·약한 프로파일 */
      p.base_level = 1;
      p.humidity_gain = 1.1f;
      p.aqi_gain = 1.05f;
      p.night_scale = 0.75f;
      p.allow_auto_start = true;
      break;
  }
  return p;
}

}  // namespace neb
