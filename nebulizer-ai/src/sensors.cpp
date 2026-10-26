#include "sensors.h"

#include "board_hal.h"
#include "config.h"

namespace neb {

void SensorHub::begin() {
  override_ = false;
  tank_pct_ = 75.0f;
  humidity_pct_ = 45.0f;
  temperature_c_ = 24.0f;
  aqi_ = 40;
  cartridge_ = true;
}

float SensorHub::adc_to_tank_pct(uint16_t adc) {
  /* 0..4095 → 0..100 (linear stub; calibrate per board) */
  float pct = (static_cast<float>(adc) / 4095.0f) * 100.0f;
  if (pct < 0.0f) pct = 0.0f;
  if (pct > 100.0f) pct = 100.0f;
  return pct;
}

float SensorHub::adc_to_humidity(uint16_t adc) {
  float rh = 20.0f + (static_cast<float>(adc) / 4095.0f) * 60.0f;
  if (rh > 100.0f) rh = 100.0f;
  return rh;
}

uint16_t SensorHub::adc_to_aqi(uint16_t adc) {
  return static_cast<uint16_t>((static_cast<float>(adc) / 4095.0f) * 200.0f);
}

SensorSnapshot SensorHub::read() {
  SensorSnapshot s{};
  if (override_) {
    s.tank_pct = tank_pct_;
    s.humidity_pct = humidity_pct_;
    s.temperature_c = temperature_c_;
    s.aqi = aqi_;
    s.cartridge_present = cartridge_;
    s.valid = true;
    return s;
  }

  const uint16_t tank_adc = hal::read_tank_adc();
  const uint16_t air_adc = hal::read_air_adc();
  s.tank_pct = adc_to_tank_pct(tank_adc);
  s.humidity_pct = adc_to_humidity(air_adc);
  s.aqi = adc_to_aqi(air_adc);
  s.temperature_c = temperature_c_;
  s.cartridge_present = hal::read_cartridge_present();
  s.valid = true;
  return s;
}

void SensorHub::set_tank_pct(float pct) {
  override_ = true;
  if (pct < 0.0f) pct = 0.0f;
  if (pct > 100.0f) pct = 100.0f;
  tank_pct_ = pct;
  const uint16_t adc = static_cast<uint16_t>((pct / 100.0f) * 4095.0f);
  hal::sim_set_tank_adc(adc);
}

void SensorHub::set_humidity_pct(float rh) {
  override_ = true;
  if (rh < 0.0f) rh = 0.0f;
  if (rh > 100.0f) rh = 100.0f;
  humidity_pct_ = rh;
}

void SensorHub::set_aqi(uint16_t aqi) {
  override_ = true;
  aqi_ = aqi;
}

void SensorHub::set_cartridge(bool present) {
  override_ = true;
  cartridge_ = present;
  hal::sim_set_cartridge(present);
}

}  // namespace neb
