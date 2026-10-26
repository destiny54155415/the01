#if defined(NEBULIZER_BOARD_ESP32)

#include "board_hal.h"

#include <Arduino.h>
#include <stdarg.h>
#include <stdio.h>

#include "pin_map.h"

namespace neb {
namespace hal {
namespace {

uint16_t g_sim_tank = 2800;
uint16_t g_sim_air = 1800;
bool g_sim_cartridge = true;
bool g_use_sim_adc = false;

}  // namespace

void init() {
  pinMode(pins::kStatusLed, OUTPUT);
  pinMode(pins::kRunLed, OUTPUT);
  pinMode(pins::kCartridgeSense, INPUT_PULLUP);
  pinMode(pins::kUserButton, INPUT_PULLUP);
  ledcSetup(pins::kPwmChannel, pins::kPwmFreqHz, pins::kPwmResolutionBits);
  ledcAttachPin(pins::kSprayPwm, pins::kPwmChannel);
  ledcWrite(pins::kPwmChannel, 0);
  set_status_led(false);
  set_run_led(false);
}

uint32_t millis() { return ::millis(); }
void delay_ms(uint32_t ms) { ::delay(ms); }

void set_spray_pwm(uint8_t duty_0_255) { ledcWrite(pins::kPwmChannel, duty_0_255); }
void set_status_led(bool on) { digitalWrite(pins::kStatusLed, on ? HIGH : LOW); }
void set_run_led(bool on) { digitalWrite(pins::kRunLed, on ? HIGH : LOW); }

uint16_t read_tank_adc() {
  if (g_use_sim_adc) return g_sim_tank;
  return analogRead(pins::kTankAdc);
}

uint16_t read_air_adc() {
  if (g_use_sim_adc) return g_sim_air;
  return analogRead(pins::kAirAdc);
}

bool read_cartridge_present() {
  if (g_use_sim_adc) return g_sim_cartridge;
  return digitalRead(pins::kCartridgeSense) == LOW;
}

bool read_user_button() { return digitalRead(pins::kUserButton) == LOW; }

void console_begin(uint32_t baud) { Serial.begin(baud); }
bool console_available() { return Serial.available() > 0; }
int console_read() { return Serial.read(); }

void console_print(const char* s) { Serial.print(s); }
void console_println(const char* s) { Serial.println(s); }

void console_printf(const char* fmt, ...) {
  char buf[192];
  va_list ap;
  va_start(ap, fmt);
  vsnprintf(buf, sizeof(buf), fmt, ap);
  va_end(ap);
  Serial.print(buf);
}

void sim_set_tank_adc(uint16_t v) {
  g_use_sim_adc = true;
  g_sim_tank = v;
}
void sim_set_air_adc(uint16_t v) {
  g_use_sim_adc = true;
  g_sim_air = v;
}
void sim_set_cartridge(bool present) {
  g_use_sim_adc = true;
  g_sim_cartridge = present;
}

}  // namespace hal
}  // namespace neb

#endif
