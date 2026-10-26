#include "voice.h"

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

bool contains_ci(const char* hay, const char* needle) {
  if (!hay || !needle || !*needle) return false;
  const size_t nlen = strlen(needle);
  for (const char* p = hay; *p; ++p) {
    size_t i = 0;
    while (i < nlen) {
      const unsigned char hc = static_cast<unsigned char>(p[i]);
      const unsigned char nc = static_cast<unsigned char>(needle[i]);
      if (!p[i] || tolower(hc) != tolower(nc)) break;
      ++i;
    }
    if (i == nlen) return true;
  }
  return false;
}

}  // namespace

void VoiceRecognizer::begin() {
  state_ = VoiceState::Idle;
  pending_ = VoiceCommand::None;
  wake_until_ms_ = 0;
}

void VoiceRecognizer::tick(uint32_t now_ms) {
  now_ms_ = now_ms;
  if (state_ != VoiceState::Idle && now_ms > wake_until_ms_) {
    state_ = VoiceState::Idle;
  }
}

void VoiceRecognizer::enter_wake(uint32_t now_ms) {
  state_ = VoiceState::AwaitingCommand;
  wake_until_ms_ = now_ms + kWakeWindowMs;
  now_ms_ = now_ms;
}

bool VoiceRecognizer::wake_active(uint32_t now_ms) const {
  return state_ != VoiceState::Idle && now_ms <= wake_until_ms_;
}

VoiceCommand VoiceRecognizer::take_command() {
  const VoiceCommand c = pending_;
  pending_ = VoiceCommand::None;
  return c;
}

bool VoiceRecognizer::is_wake_token(const char* t) {
  return streq_ci(t, "hey") || streq_ci(t, "neb") || streq_ci(t, "nebulizer") ||
         streq_ci(t, "네뷸") || streq_ci(t, "헤이");
}

VoiceCommand VoiceRecognizer::parse_command_token(const char* t) {
  if (streq_ci(t, "start") || streq_ci(t, "on") || streq_ci(t, "시작") || streq_ci(t, "켜줘")) {
    return VoiceCommand::Start;
  }
  if (streq_ci(t, "stop") || streq_ci(t, "off") || streq_ci(t, "정지") || streq_ci(t, "꺼줘")) {
    return VoiceCommand::Stop;
  }
  if (streq_ci(t, "auto") || streq_ci(t, "자동")) return VoiceCommand::ModeAuto;
  if (streq_ci(t, "manual") || streq_ci(t, "수동")) return VoiceCommand::ModeManual;
  if (streq_ci(t, "up") || streq_ci(t, "louder") || streq_ci(t, "세게") || streq_ci(t, "올려")) {
    return VoiceCommand::LevelUp;
  }
  if (streq_ci(t, "down") || streq_ci(t, "softer") || streq_ci(t, "약하게") || streq_ci(t, "내려")) {
    return VoiceCommand::LevelDown;
  }
  if (streq_ci(t, "1") || streq_ci(t, "level1") || streq_ci(t, "1단")) return VoiceCommand::LevelSet1;
  if (streq_ci(t, "2") || streq_ci(t, "level2") || streq_ci(t, "2단")) return VoiceCommand::LevelSet2;
  if (streq_ci(t, "3") || streq_ci(t, "level3") || streq_ci(t, "3단")) return VoiceCommand::LevelSet3;
  if (streq_ci(t, "4") || streq_ci(t, "level4") || streq_ci(t, "4단")) return VoiceCommand::LevelSet4;
  if (streq_ci(t, "5") || streq_ci(t, "level5") || streq_ci(t, "5단")) return VoiceCommand::LevelSet5;
  if (streq_ci(t, "status") || streq_ci(t, "상태")) return VoiceCommand::Status;
  return VoiceCommand::None;
}

void VoiceRecognizer::normalize(char* dst, size_t dst_len, const char* src) {
  if (!dst || dst_len == 0) return;
  size_t o = 0;
  for (const char* p = src; *p && o + 1 < dst_len; ++p) {
    const unsigned char c = static_cast<unsigned char>(*p);
    if (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == ',' || c == '.') continue;
    /* ASCII fold only — keep UTF-8 Korean bytes intact */
    dst[o++] = (c < 0x80) ? static_cast<char>(tolower(c)) : static_cast<char>(c);
  }
  dst[o] = '\0';
}

void VoiceRecognizer::feed_keyword(const char* token) {
  if (!token || !*token) return;
  char buf[48];
  normalize(buf, sizeof(buf), token);

  /* For UTF-8 Korean, keep original for parse when ascii normalize emptied meaningful chars */
  const char* use = buf[0] ? buf : token;

  if (is_wake_token(use) || is_wake_token(token)) {
    enter_wake(now_ms_);
    return;
  }

  if (state_ == VoiceState::Idle) {
    /* Allow direct commands in demo; production can require wake */
    const VoiceCommand c = parse_command_token(use);
    if (c == VoiceCommand::None) {
      const VoiceCommand c2 = parse_command_token(token);
      if (c2 != VoiceCommand::None) pending_ = c2;
    } else {
      pending_ = c;
    }
    return;
  }

  const VoiceCommand c = parse_command_token(use);
  if (c != VoiceCommand::None) {
    pending_ = c;
    state_ = VoiceState::Idle;
    return;
  }
  const VoiceCommand c2 = parse_command_token(token);
  if (c2 != VoiceCommand::None) {
    pending_ = c2;
    state_ = VoiceState::Idle;
  }
}

void VoiceRecognizer::feed_asr_hypothesis(const char* text, bool is_final) {
  if (!text || !*text) return;
  if (!is_final) return;  // partials ignored in stub; real NLU may stream

  /* Offline phrase → keyword map (EN / KO) */
  if (contains_ci(text, "hey neb") || contains_ci(text, "hey nebulizer") ||
      strstr(text, "헤이 네뷸") || strstr(text, "네뷸아")) {
    feed_keyword("neb");
  }
  if (contains_ci(text, "start") || contains_ci(text, "turn on") || strstr(text, "시작해") ||
      strstr(text, "켜 줘") || strstr(text, "켜줘")) {
    feed_keyword("start");
  } else if (contains_ci(text, "stop") || contains_ci(text, "turn off") || strstr(text, "멈춰") ||
             strstr(text, "꺼 줘") || strstr(text, "꺼줘")) {
    feed_keyword("stop");
  } else if (contains_ci(text, "auto mode") || strstr(text, "자동 모드") || strstr(text, "자동모드")) {
    feed_keyword("auto");
  } else if (contains_ci(text, "manual") || strstr(text, "수동")) {
    feed_keyword("manual");
  } else if (contains_ci(text, "level up") || contains_ci(text, "stronger") || strstr(text, "세게") ||
             strstr(text, "올려")) {
    feed_keyword("up");
  } else if (contains_ci(text, "level down") || contains_ci(text, "softer") || strstr(text, "약하게") ||
             strstr(text, "내려")) {
    feed_keyword("down");
  } else if (strstr(text, "1단") || contains_ci(text, "level 1")) {
    feed_keyword("1");
  } else if (strstr(text, "2단") || contains_ci(text, "level 2")) {
    feed_keyword("2");
  } else if (strstr(text, "3단") || contains_ci(text, "level 3")) {
    feed_keyword("3");
  } else if (strstr(text, "4단") || contains_ci(text, "level 4")) {
    feed_keyword("4");
  } else if (strstr(text, "5단") || contains_ci(text, "level 5")) {
    feed_keyword("5");
  } else if (contains_ci(text, "status") || strstr(text, "상태")) {
    feed_keyword("status");
  }
}

}  // namespace neb
