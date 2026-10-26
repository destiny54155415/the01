# Host build without PlatformIO (demo + smoke). ESP32 flash still uses PlatformIO.
CXX ?= g++
CXXFLAGS ?= -std=c++17 -Wall -Wextra -Iinclude -DNEBULIZER_BOARD_NATIVE=1 -DNEBULIZER_SIM_CONSOLE=1 -DNEBULIZER_FW_VERSION=\"1.0.0-host\"
SRC = src/voice.cpp src/driving_mode.cpp src/spray_control.cpp src/sensors.cpp \
      src/demo_console.cpp src/nebulizer_app.cpp src/hal/board_hal_native.cpp
OBJ_DIR = build/native

.PHONY: all demo smoke clean

all: demo smoke

$(OBJ_DIR):
	mkdir -p $(OBJ_DIR)

demo: $(OBJ_DIR) $(SRC) src/main.cpp
	$(CXX) $(CXXFLAGS) -o $(OBJ_DIR)/nebulizer_demo $(SRC) src/main.cpp -pthread
	@echo "Run: $(OBJ_DIR)/nebulizer_demo"

smoke: $(OBJ_DIR) $(SRC) test/host_smoke.cpp
	$(CXX) $(CXXFLAGS) -o $(OBJ_DIR)/host_smoke $(SRC) test/host_smoke.cpp -pthread
	$(OBJ_DIR)/host_smoke

clean:
	rm -rf build
