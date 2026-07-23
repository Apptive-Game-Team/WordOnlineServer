# 2026-07-11 — 봇 페르소나를 음수 사용자 identity에 연결

- Date: 2026-07-11
- GitHub Issue: #288
- Status: Implemented

## Goal

음수 `users.id`를 game participant와 persistence의 단일 bot identity로 사용한다. Persona는 이름과 AI 행동 설정만 보유하고, MMR과 선택 deck은 연결된 user에서 읽고 쓴다.

## Non-goals

- DB production migration 구현(database #8)
- Lobby 표시 이름 및 Admin UI 구현

## Context / Constraints

- 현재 `BotParticipant`는 persona ID를 음수 participant ID로 변환한다.
- `DeckService`는 persona의 `deck_id`, `MmrService`는 persona의 `mmr`를 사용한다.
- 기존 서버와 신규 schema의 순차 배포 호환성이 필요하다.

## Approach (Checklist)

- [x] **Step 0: Recon** (`BotParticipant`, persona repository/service/controller, scheduler, deck/MMR, loop와 tests 재검증)
- [x] **Step 1: Domain contract** (`BotPersona.userId/name/behavior/enabled`; deck/MMR 제거, 음수 user 검증)
- [x] **Step 2: Persistence/API** (user_id 기반 query/CRUD DTO, not-found/validation/delete 계약)
- [x] **Step 3: Runtime** (scheduler/session/agent가 음수 user ID를 그대로 사용; selected deck/MMR을 user repository에서 조회)
- [x] **Step 4: Tests** (persona lookup, bot/human deck, MMR, scheduler, CRUD, H2 schema)
- [x] **Step 5: Rollout / Rollback** (database #8 선행, 구 schema fallback 필요성 판단, app rollback)
- [x] **Step 6: Debug practice random bot** (`/game/practice`에서 enabled persona 무작위 선택, `SessionType.Practice`, PVE 경로 분리)
- [x] **Step 7: Debug tests** (랜덤 bot ID 전달, 활성 bot 없음, PVE regression)

## Validation

- **Commands to run:** `./gradlew test`; bot-vs-bot 및 human-vs-bot session focused tests
- **Expected output:** 음수 user ID 변환 없이 persona/selected deck/MMR이 동일 user 기준으로 조회·갱신됨

## Risks & Rollback

- **Risks:** 기존 persona ID와 user ID가 다른 데이터 backfill; default persona가 연결 끊긴 인간 대리 bot에도 쓰이는 경로; admin API delete 동시 진행 중 session
- **Rollback steps:** database additive schema 유지 후 game server 이전 버전 배포. 데이터 삭제 금지.

## Open Questions

- 연결 끊긴 실제 유저 대리 AI는 기존 `BotPersona.DEFAULT`를 유지한다.
- Game server는 persona CRUD API를 유지한다. 전체 bot user/deck/persona 생성 원자성은 Admin DB service가 소유한다.
