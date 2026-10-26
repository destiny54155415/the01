#pragma once

#include "types.h"

#include <stddef.h>
#include <stdint.h>

namespace neb {

/**
 * On-device voice path: wake + command keyword FSM.
 * ASR/NLU hooks are stubs — swap feed_asr_token() for a real model decoder.
 */
class VoiceRecognizer {
 public:
  void begin();
  void tick(uint32_t now_ms);

  /** Feed a normalized keyword token (from demo console or ASR stub). */
  void feed_keyword(const char* token);

  /**
   * Hook for a real ASR/NLU backend: pass partial/final text hypothesis.
   * Parses Korean/English offline command set into keywords.
   */
  void feed_asr_hypothesis(const char* text, bool is_final);

  VoiceState state() const { return state_; }
  VoiceCommand take_command();  // clears pending
  bool wake_active(uint32_t now_ms) const;

 private:
  VoiceState state_ = VoiceState::Idle;
  VoiceCommand pending_ = VoiceCommand::None;
  uint32_t wake_until_ms_ = 0;
  uint32_t now_ms_ = 0;

  void enter_wake(uint32_t now_ms);
  static bool is_wake_token(const char* t);
  static VoiceCommand parse_command_token(const char* t);
  static void normalize(char* dst, size_t dst_len, const char* src);
};

}  // namespace neb
