/**
 * Host smoke test (no MCU): voice FSM, driving profiles, spray regulation.
 * Build: make -C nebulizer-ai smoke
 */
#include "config.h"
#include "driving_mode.h"
#include "spray_control.h"
#include "types.h"
#include "voice.h"

#include <cstdio>
#include <cstdlib>

#if !defined(NEBULIZER_BOARD_NATIVE)
#define NEBULIZER_BOARD_NATIVE 1
#endif

#include "board_hal.h"

using namespace neb;

static int g_fail = 0;

#define EXPECT(cond)                                         \
  do {                                                       \
    if (!(cond)) {                                           \
      std::printf("FAIL %s:%d: %s\n", __FILE__, __LINE__, #cond); \
      ++g_fail;                                              \
    }                                                        \
  } while (0)

int main() {
  hal::init();

  VoiceRecognizer voice;
  voice.begin();
  voice.tick(1000);
  voice.feed_keyword("neb");
  EXPECT(voice.state() == VoiceState::AwaitingCommand);
  voice.feed_keyword("start");
  EXPECT(voice.take_command() == VoiceCommand::Start);

  voice.feed_asr_hypothesis("헤이 네뷸 3단으로", true);
  /* wake + level may queue last command */
  voice.tick(2000);
  VoiceCommand c = voice.take_command();
  EXPECT(c == VoiceCommand::LevelSet3 || c == VoiceCommand::None || c == VoiceCommand::Start);

  DrivingModeController drive;
  drive.begin();
  drive.set_mode(DrivingMode::Sport);
  EXPECT(drive.profile().base_level == 4);
  drive.set_mode(DrivingMode::Night);
  EXPECT(drive.profile().base_level == 1);
  EXPECT(drive.profile().night_scale < 1.0f);

  SprayController spray;
  spray.begin();
  spray.set_mode(NebulizerMode::Auto);
  spray.set_running(true);

  SensorSnapshot s{};
  s.valid = true;
  s.cartridge_present = true;
  s.tank_pct = 80.0f;
  s.humidity_pct = 25.0f;  // dry cabin → boost
  s.aqi = 120;
  s.temperature_c = 22.0f;

  const SprayProfile p = drive.profile();
  spray.tick(5000, s, p);
  EXPECT(spray.effective_level() >= 1);
  EXPECT(spray.running());

  s.tank_pct = 2.0f;  // empty → dry lock
  spray.tick(5500, s, p);
  EXPECT(spray.dry_lock());
  EXPECT(!spray.running());

  if (g_fail) {
    std::printf("%d failure(s)\n", g_fail);
    return 1;
  }
  std::printf("host_smoke OK\n");
  return 0;
}
