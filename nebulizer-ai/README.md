# 네뷸라이저 AI 펌웨어 (`nebulizer-ai/`)

차량·캐빈용 네뷸라이저 PCB에 올리는 **온디바이스 AI 제어 펌웨어**입니다.  
MCU는 Arduino 호환 HAL로 추상화했고, **ESP32급을 레퍼런스 보드**로 둡니다. 핀맵만 바꾸면 다른 공통 MCU에도 이식할 수 있습니다.

`01/`, `start01/`, `start02/` Git 연습 파일은 건드리지 않습니다.

## Cursor에서 열기

1. 이 저장소를 Cursor에서 Clone / Open 합니다. (`github.com/destiny54155415/the01`)
2. **File → Open Folder** 로 저장소 루트 또는 `nebulizer-ai/` 폴더를 엽니다.
3. PlatformIO 확장이 있으면 `platformio.ini`가 프로젝트로 인식됩니다.
4. 시리얼 데모만 보려면 터미널에서:

```bash
cd nebulizer-ai
make smoke    # 로직 스모크 테스트
make demo     # 콘솔 데모 바이너리
./build/native/nebulizer_demo
```

## 폴더 구조

```
nebulizer-ai/
  platformio.ini          # ESP32 + native env
  Makefile                # 호스트 데모/스모크 (PIO 없이)
  include/                # 공개 헤더 (config, pin_map, AI 모듈)
  src/                    # main, voice, driving_mode, spray_control, sensors, demo
  src/hal/                # ESP32 / native 보드 계층
  test/host_smoke.cpp     # 하드웨어 없는 검증
```

## AI 기능 맵

| 기능 | 모듈 | 동작 |
|------|------|------|
| **음성 인식** | `voice.*` | 웨이크워드 + 명령 키워드 FSM. `feed_asr_hypothesis()`는 실제 ASR/NLU 연결 훅. 한/영 오프라인 명령셋. |
| **주행 모드 자동 전환** | `driving_mode.*` | `eco` / `comfort` / `sport` / `night` → 분사 프로파일(기본 단수·게인·야간 스케일) 자동 적용. |
| **분사량 자동 조절** | `spray_control.*` | 탱크·습도·AQI 스텁 + 주행 프로파일로 단수 폐루프/규칙 제어. 메시 보호(최대 온타임·쿨다운). |

## 핀맵 (ESP32 레퍼런스)

| 신호 | GPIO | 설명 |
|------|------|------|
| Spray PWM | 25 | 피에조/메시 구동 |
| Status LED | 26 | 상태 |
| Run LED | 27 | 분사 중 |
| Tank ADC | 34 | 탱크 잔량 |
| Air ADC | 35 | 습도/AQI 스텁 |
| Cartridge | 32 | 카트리지 감지 (LOW=장착) |
| User button | 33 | 선택 |
| Mic I2S | 4 / 5 / 18 | ASR 훅용 (키워드 스텁에서는 미사용) |

상세 주석은 `include/pin_map.h`를 보세요.

## 빌드 · 플래시 (PlatformIO)

```bash
cd nebulizer-ai
pio run -e esp32dev           # 빌드
pio run -e esp32dev -t upload # 플래시
pio device monitor -b 115200  # 시리얼
```

Arduino IDE를 쓸 경우: ESP32 보드 패키지 설치 후 `src/`·`include/` 소스를 스케치로 가져오고, `NEBULIZER_BOARD_ESP32`를 정의하세요. PlatformIO 사용을 권장합니다.

호스트(네이티브) 로직만:

```bash
pio run -e native
# 또는
make demo
```

## 하드웨어 없이 데모 (시리얼 콘솔)

보드 또는 `make demo` 실행 후 입력:

```
help
voice neb
voice start
drive sport
humidity 25
aqi 150
status
tank 3
status
drive night
asr 헤이 네뷸 약하게
```

| 명령 | 의미 |
|------|------|
| `voice <kw>` | 웨이크/명령 키워드 주입 (`neb`, `start`, `stop`, `auto`, `1`…`5`, `세게`…) |
| `asr <text>` | ASR 가설 문장 (한/영 구문) |
| `drive <mode>` | `eco` `comfort` `sport` `night` |
| `tank` / `humidity` / `aqi` / `cart` | 센서 스텁 |
| `start` `stop` `mode` `level` | 직접 제어 |

## 식별자 / 주석

코드 식별자는 영어입니다. 도메인 용어(절약·야간 프로파일 등)만 짧은 한국어 주석을 달았습니다.
