# AI 기능 요약

## 1. 음성 인식 (`src/voice.cpp`)

- 웨이크워드: `neb` / `hey` / `네뷸` / `헤이`
- 명령: start/stop, auto/manual, level up/down / 1–5단, status (한·영)
- `feed_asr_hypothesis()` — 실제 온디바이스 ASR·NLU 모델이 최종 텍스트를 넘기는 훅
- 데모: `voice start`, `asr 헤이 네뷸 켜줘`

## 2. 주행 모드 자동 전환 (`src/driving_mode.cpp`)

| 모드 | 기본 단 | 특징 |
|------|---------|------|
| eco | 1 | 절약, 자동 시작 억제 |
| comfort | 2 | 기본 캐빈 프로파일 |
| sport | 4 | 강한 분사, AQI 반응↑ |
| night | 1 | night_scale 0.75, 조용 |

앱 루프에서 모드 변경 시 프로파일을 네뷸라이저에 반영합니다.

## 3. 분사량 자동 조절 (`src/spray_control.cpp`)

입력: 탱크 %, 습도, AQI, 주행 프로파일 → 출력 단수 1–5.  
빈 탱크 dry-lock, 메시 최대 온타임·쿨다운 보호. Auto에서 건조 시 부스트, 고습 시 완화.
