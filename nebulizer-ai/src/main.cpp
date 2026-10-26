/**
 * Nebulizer AI firmware entry.
 * ESP32 Arduino: setup/loop. Native host: main() runs the same app loop.
 */

#include "nebulizer_app.h"

namespace {
neb::NebulizerApp g_app;
}

#if defined(NEBULIZER_BOARD_NATIVE)

int main() {
  g_app.begin();
  for (;;) {
    g_app.loop();
  }
  return 0;
}

#else

#include <Arduino.h>

void setup() { g_app.begin(); }

void loop() { g_app.loop(); }

#endif
