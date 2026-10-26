#pragma once

/**
 * Board-agnostic pin names. ESP32-class reference mapping below.
 * Remap in board_hal_* for other Arduino-compatible MCUs.
 *
 * ESP32 DevKit (reference):
 *   GPIO 25  — piezo / mesh drive PWM (spray actuator)
 *   GPIO 26  — status LED
 *   GPIO 27  — power / run indicator
 *   GPIO 34  — tank level ADC (input only)
 *   GPIO 35  — humidity/AQI stub ADC (or I2C in real HW)
 *   GPIO 32  — cartridge present digital
 *   GPIO 33  — user button (optional)
 *   GPIO 4   — I2S/mic data (ASR hook; unused in keyword stub)
 *   GPIO 5   — I2S/mic BCLK
 *   GPIO 18  — I2S/mic WS
 */

namespace neb {
namespace pins {

#if defined(NEBULIZER_BOARD_ESP32)
constexpr int kSprayPwm = 25;
constexpr int kStatusLed = 26;
constexpr int kRunLed = 27;
constexpr int kTankAdc = 34;
constexpr int kAirAdc = 35;
constexpr int kCartridgeSense = 32;
constexpr int kUserButton = 33;
constexpr int kMicData = 4;
constexpr int kMicBclk = 5;
constexpr int kMicWs = 18;
constexpr int kPwmChannel = 0;
constexpr int kPwmFreqHz = 40000;
constexpr int kPwmResolutionBits = 8;
#else
/* Native / unspecified: logical pin ids for HAL stubs */
constexpr int kSprayPwm = 1;
constexpr int kStatusLed = 2;
constexpr int kRunLed = 3;
constexpr int kTankAdc = 4;
constexpr int kAirAdc = 5;
constexpr int kCartridgeSense = 6;
constexpr int kUserButton = 7;
constexpr int kMicData = 8;
constexpr int kMicBclk = 9;
constexpr int kMicWs = 10;
constexpr int kPwmChannel = 0;
constexpr int kPwmFreqHz = 40000;
constexpr int kPwmResolutionBits = 8;
#endif

}  // namespace pins
}  // namespace neb
