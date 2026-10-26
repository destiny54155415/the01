#pragma once

#include "types.h"

#include <stdint.h>

namespace neb {

/**
 * Driver / vehicle mode → nebulizer profile mapping.
 * External source: CAN/UART stub via set_mode() or demo console.
 */
class DrivingModeController {
 public:
  void begin();
  void set_mode(DrivingMode mode);
  DrivingMode mode() const { return mode_; }

  /** Profile for current driving mode (base level, gains). */
  SprayProfile profile() const;

  /** True when mode changed since last consume. */
  bool consume_changed();

  static const char* mode_name(DrivingMode m);
  static bool parse_name(const char* name, DrivingMode* out);

 private:
  DrivingMode mode_ = DrivingMode::Comfort;
  bool changed_ = true;
};

}  // namespace neb
