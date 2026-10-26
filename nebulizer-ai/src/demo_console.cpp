#include "demo_console.h"

#include "board_hal.h"

#include <stdlib.h>
#include <string.h>

namespace neb {
namespace {

const char* voice_cmd_name(VoiceCommand c) {
  switch (c) {
    case VoiceCommand::Start:
      return "start";
    case VoiceCommand::Stop:
      return "stop";
    case VoiceCommand::ModeAuto:
      return "auto";
    case VoiceCommand::ModeManual:
      return "manual";
    case VoiceCommand::LevelUp:
      return "up";
    case VoiceCommand::LevelDown:
      return "down";
    case VoiceCommand::LevelSet1:
      return "1";
    case VoiceCommand::LevelSet2:
      return "2";
    case VoiceCommand::LevelSet3:
      return "3";
    case VoiceCommand::LevelSet4:
      return "4";
    case VoiceCommand::LevelSet5:
      return "5";
    case VoiceCommand::Status:
      return "status";
    default:
      return "none";
  }
}

}  // namespace

void DemoConsole::begin(VoiceRecognizer* voice, DrivingModeController* drive, SensorHub* sensors,
                        SprayController* spray, DeviceState* state) {
  voice_ = voice;
  drive_ = drive;
  sensors_ = sensors;
  spray_ = spray;
  state_ = state;
  len_ = 0;
  line_[0] = '\0';
  print_help();
}

void DemoConsole::print_help() const {
  hal::console_println("=== Nebulizer AI demo console ===");
  hal::console_println("help                 — 이 도움말");
  hal::console_println("status               — 현재 상태");
  hal::console_println("voice <kw>           — 음성 키워드 주입 (hey/neb/start/stop/auto/...)");
  hal::console_println("asr <text>           — ASR 가설 문장 (한/영)");
  hal::console_println("drive <mode>         — eco|comfort|sport|night");
  hal::console_println("tank <0-100>         — 탱크 잔량 %");
  hal::console_println("humidity <0-100>     — 실내 습도 %");
  hal::console_println("aqi <n>              — 공기질 지수");
  hal::console_println("cart on|off          — 카트리지 장착");
  hal::console_println("start|stop           — 분사 시작/정지");
  hal::console_println("mode auto|manual|off — 운전 모드");
  hal::console_println("level <1-5>          — 수동 단수");
}

void DemoConsole::print_status() const {
  if (!spray_ || !drive_ || !sensors_ || !voice_) return;
  const SensorSnapshot sens = sensors_->read();
  hal::console_printf(
      "run=%d mode=%d level=%u eff=%u drive=%s tank=%.0f rh=%.0f aqi=%u cart=%d dry=%d cool=%d "
      "voice=%d\n",
      spray_->running() ? 1 : 0, static_cast<int>(spray_->mode()), spray_->commanded_level(),
      spray_->effective_level(), DrivingModeController::mode_name(drive_->mode()),
      static_cast<double>(sens.tank_pct), static_cast<double>(sens.humidity_pct), sens.aqi,
      sens.cartridge_present ? 1 : 0, spray_->dry_lock() ? 1 : 0, spray_->mesh_cooling() ? 1 : 0,
      static_cast<int>(voice_->state()));
  (void)state_;
}

void DemoConsole::poll() {
  while (hal::console_available()) {
    const int ch = hal::console_read();
    if (ch < 0) break;
    if (ch == '\r') continue;
    if (ch == '\n') {
      line_[len_] = '\0';
      if (len_ > 0) handle_line(line_);
      len_ = 0;
      continue;
    }
    if (len_ + 1 < sizeof(line_)) {
      line_[len_++] = static_cast<char>(ch);
    }
  }
}

void DemoConsole::handle_line(const char* line) {
  char buf[96];
  strncpy(buf, line, sizeof(buf) - 1);
  buf[sizeof(buf) - 1] = '\0';

  char* cmd = strtok(buf, " \t");
  char* arg = strtok(nullptr, "");
  if (!cmd) return;
  while (arg && (*arg == ' ' || *arg == '\t')) ++arg;

  if (strcmp(cmd, "help") == 0 || strcmp(cmd, "?") == 0) {
    print_help();
    return;
  }
  if (strcmp(cmd, "status") == 0) {
    print_status();
    return;
  }
  if ((strcmp(cmd, "voice") == 0 || strcmp(cmd, "asr") == 0) && arg && voice_) {
    if (strcmp(cmd, "asr") == 0) {
      voice_->feed_asr_hypothesis(arg, true);
      hal::console_printf("asr <- '%s'\n", arg);
    } else {
      voice_->feed_keyword(arg);
      hal::console_printf("voice <- '%s'\n", arg);
    }
    /* Apply immediately so status in the same console batch is accurate */
    const VoiceCommand vc = voice_->take_command();
    if (spray_ && vc != VoiceCommand::None) {
      switch (vc) {
        case VoiceCommand::Start:
          spray_->set_running(true);
          break;
        case VoiceCommand::Stop:
          spray_->set_running(false);
          break;
        case VoiceCommand::ModeAuto:
          spray_->set_mode(NebulizerMode::Auto);
          break;
        case VoiceCommand::ModeManual:
          spray_->set_mode(NebulizerMode::Manual);
          break;
        case VoiceCommand::LevelUp:
          spray_->set_mode(NebulizerMode::Manual);
          spray_->nudge_level(+1);
          break;
        case VoiceCommand::LevelDown:
          spray_->set_mode(NebulizerMode::Manual);
          spray_->nudge_level(-1);
          break;
        case VoiceCommand::LevelSet1:
        case VoiceCommand::LevelSet2:
        case VoiceCommand::LevelSet3:
        case VoiceCommand::LevelSet4:
        case VoiceCommand::LevelSet5: {
          const uint8_t lvl =
              static_cast<uint8_t>(vc) - static_cast<uint8_t>(VoiceCommand::LevelSet1) + 1;
          spray_->set_mode(NebulizerMode::Manual);
          spray_->set_manual_level(lvl);
          spray_->set_running(true);
          break;
        }
        case VoiceCommand::Status:
          print_status();
          break;
        default:
          break;
      }
    }
    return;
  }
  if (strcmp(cmd, "drive") == 0 && arg && drive_) {
    DrivingMode m;
    if (DrivingModeController::parse_name(arg, &m)) {
      drive_->set_mode(m);
      hal::console_printf("drive -> %s\n", DrivingModeController::mode_name(m));
    } else {
      hal::console_println("unknown drive mode");
    }
    return;
  }
  if (strcmp(cmd, "tank") == 0 && arg && sensors_) {
    sensors_->set_tank_pct(static_cast<float>(atof(arg)));
    return;
  }
  if (strcmp(cmd, "humidity") == 0 && arg && sensors_) {
    sensors_->set_humidity_pct(static_cast<float>(atof(arg)));
    return;
  }
  if (strcmp(cmd, "aqi") == 0 && arg && sensors_) {
    sensors_->set_aqi(static_cast<uint16_t>(atoi(arg)));
    return;
  }
  if (strcmp(cmd, "cart") == 0 && arg && sensors_) {
    sensors_->set_cartridge(strcmp(arg, "on") == 0 || strcmp(arg, "1") == 0);
    return;
  }
  if (strcmp(cmd, "start") == 0 && spray_) {
    spray_->set_running(true);
    return;
  }
  if (strcmp(cmd, "stop") == 0 && spray_) {
    spray_->set_running(false);
    return;
  }
  if (strcmp(cmd, "mode") == 0 && arg && spray_) {
    if (strcmp(arg, "auto") == 0) spray_->set_mode(NebulizerMode::Auto);
    else if (strcmp(arg, "manual") == 0) spray_->set_mode(NebulizerMode::Manual);
    else if (strcmp(arg, "off") == 0) spray_->set_mode(NebulizerMode::Off);
    return;
  }
  if (strcmp(cmd, "level") == 0 && arg && spray_) {
    spray_->set_manual_level(static_cast<uint8_t>(atoi(arg)));
    spray_->set_mode(NebulizerMode::Manual);
    return;
  }

  hal::console_println("unknown command — type help");
  (void)voice_cmd_name;
}

}  // namespace neb
