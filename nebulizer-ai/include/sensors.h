#pragma once

#include "types.h"

#include <stdint.h>

namespace neb {

class SensorHub {
 public:
  void begin();
  SensorSnapshot read();

  /** Demo overrides (also route through HAL sim injectors). */
  void set_tank_pct(float pct);
  void set_humidity_pct(float rh);
  void set_aqi(uint16_t aqi);
  void set_cartridge(bool present);

 private:
  bool override_ = false;
  float tank_pct_ = 75.0f;
  float humidity_pct_ = 45.0f;
  float temperature_c_ = 24.0f;
  uint16_t aqi_ = 40;
  bool cartridge_ = true;

  static float adc_to_tank_pct(uint16_t adc);
  static float adc_to_humidity(uint16_t adc);
  static uint16_t adc_to_aqi(uint16_t adc);
};

}  // namespace neb
