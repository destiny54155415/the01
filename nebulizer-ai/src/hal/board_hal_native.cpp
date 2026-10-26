#if defined(NEBULIZER_BOARD_NATIVE)

#include "board_hal.h"

#include <chrono>
#include <cstdarg>
#include <cstdio>
#include <cstring>
#include <thread>

namespace neb {
namespace hal {
namespace {

using clock = std::chrono::steady_clock;
clock::time_point g_start = clock::now();

uint16_t g_tank = 2800;
uint16_t g_air = 1800;
bool g_cartridge = true;
uint8_t g_pwm = 0;
bool g_status = false;
bool g_run = false;

char g_line[128];
size_t g_line_len = 0;
bool g_line_ready = false;
size_t g_line_pos = 0;

}  // namespace

void init() {
  g_start = clock::now();
  g_pwm = 0;
  g_status = false;
  g_run = false;
}

uint32_t millis() {
  auto ms = std::chrono::duration_cast<std::chrono::milliseconds>(clock::now() - g_start);
  return static_cast<uint32_t>(ms.count());
}

void delay_ms(uint32_t ms) { std::this_thread::sleep_for(std::chrono::milliseconds(ms)); }

void set_spray_pwm(uint8_t duty_0_255) { g_pwm = duty_0_255; }
void set_status_led(bool on) { g_status = on; }
void set_run_led(bool on) { g_run = on; }

uint16_t read_tank_adc() { return g_tank; }
uint16_t read_air_adc() { return g_air; }
bool read_cartridge_present() { return g_cartridge; }
bool read_user_button() { return false; }

void console_begin(uint32_t /*baud*/) {
  setvbuf(stdout, nullptr, _IONBF, 0);
}

bool console_available() {
  if (g_line_ready && g_line_pos < g_line_len) return true;
  if (g_line_ready && g_line_pos >= g_line_len) {
    g_line_ready = false;
    g_line_pos = 0;
    g_line_len = 0;
  }
  if (!fgets(g_line, sizeof(g_line), stdin)) return false;
  g_line_len = strlen(g_line);
  g_line_pos = 0;
  g_line_ready = true;
  return g_line_len > 0;
}

int console_read() {
  if (!console_available()) return -1;
  return static_cast<unsigned char>(g_line[g_line_pos++]);
}

void console_print(const char* s) { fputs(s, stdout); }
void console_println(const char* s) {
  fputs(s, stdout);
  fputc('\n', stdout);
}

void console_printf(const char* fmt, ...) {
  va_list ap;
  va_start(ap, fmt);
  vfprintf(stdout, fmt, ap);
  va_end(ap);
}

void sim_set_tank_adc(uint16_t v) { g_tank = v; }
void sim_set_air_adc(uint16_t v) { g_air = v; }
void sim_set_cartridge(bool present) { g_cartridge = present; }

}  // namespace hal
}  // namespace neb

#endif
