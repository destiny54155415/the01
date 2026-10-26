#pragma once

#include <stdint.h>

namespace neb {
namespace hal {

/** Portable Arduino-compatible board layer. ESP32 is the reference target. */

void init();
uint32_t millis();
void delay_ms(uint32_t ms);

void set_spray_pwm(uint8_t duty_0_255);
void set_status_led(bool on);
void set_run_led(bool on);

/** Raw ADC 0–4095 (ESP32 12-bit style); native returns stub values. */
uint16_t read_tank_adc();
uint16_t read_air_adc();
bool read_cartridge_present();
bool read_user_button();

/** Console I/O for demo / diagnostics. */
void console_begin(uint32_t baud);
bool console_available();
int console_read();
void console_print(const char* s);
void console_println(const char* s);
void console_printf(const char* fmt, ...);

/** Inject sensor stubs (demo console / unit tests). */
void sim_set_tank_adc(uint16_t v);
void sim_set_air_adc(uint16_t v);
void sim_set_cartridge(bool present);

}  // namespace hal
}  // namespace neb
