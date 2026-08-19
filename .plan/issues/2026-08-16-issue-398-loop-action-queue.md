# 2026-08-16 — 게임 루프 액션 큐로 입력/봇 액션 직렬화

- Date: 2026-08-16
- GitHub Issue: #398
- Status: Done

## Goal

게임 상태를 바꾸는 스레드를 루프 스레드 하나로 좁힌다. 다른 스레드는 큐에 넣기만 하고,
루프가 프레임 시작에 소비한다. 그 결과로 쓰기 경로의 `synchronized` 를 걷어낸다.

## Non-goals

- 봇의 **읽기** 동시성. `BotEye` / `BotBrain` 은 여전히 봇 executor 스레드에서 라이브
  `GameObject` 를 본다. `GameSessionData.gameObjects` 의 `CopyOnWriteArrayList`,
  `BotEye` 의 `synchronized`, `BotAgent` 의 `synchronized` 는 그대로 둔다 → #399.
- `SessionService.createSession`, `ServerStatusService` 의 `synchronized`. 게임 루프와
  무관한 락이다.
- 클라이언트 프로토콜 변경. `InputResponseDto` 모양과 목적지 토픽은 그대로다.

## Context / Constraints

- 지금은 `GameLoop.runLoop()` 이 `synchronized (gameContext)` 로 프레임 전체를 감싸고,
  `MagicInputHandler` 의 두 진입점이 같은 모니터를 잡는다. 세션 하나에 락 하나라 입력
  스레드가 최대 한 프레임을 통째로 기다린다.
- `GameLoop` 주석이 이미 이 교체를 예고하고 있었다: *"Upgrade path: drain casts from a
  queue at the top of update()"*.
- 모니터가 **덮지 못하던** 구멍이 하나 있다. `InputController` 의 `selectCard` /
  `unselectCard` / `cancelCard` 는 락 없이 `CardSelectVisualizer` 를 호출해 라이브
  `GameObject` 의 컴포넌트를 추가·취소했다. `ComponentUpdateSystem` 과 실제로 경쟁한다.
- `useMagic` 응답은 지금 STOMP 스레드가 동기 리턴값을 받아 보낸다. 큐로 옮기면 인라인
  완료가 사라지므로 전송 주체가 루프로 바뀌어야 한다.
- 핑 타임아웃 콜백(`PingChecker` 스케줄러 스레드)이 `activateBotForUser` 를 호출해
  루프가 읽는 봇 에이전트를 갈아끼운다. 이것도 오프루프 쓰기다.
- `PVEBossMob` 은 이미 루프 스레드에서 캐스팅한다. 큐를 태우지 않고 직접 호출을 유지한다.

## Approach (Checklist)

- [x] `GameActionQueue` 추가. 다수 생산자 / 단일 소비자, 용량 256 유계, 드레인 시작
      시점의 개수만 처리(액션이 액션을 넣어도 프레임이 늘어나지 않음), 액션 예외는
      개별로 잡아 프레임을 죽이지 않음.
- [x] `GameContext.submitAction` / `drainActions` 로 노출. 큐는 세션 하나당 하나.
- [x] `GameLoop.runLoop()` 에서 `synchronized (gameContext)` 제거, `drainActions()` 를
      `update()` 직전에 호출.
- [x] `InputController`: `useMagic` / `selectCard` / `unselectCard` / `cancelCard` 를
      큐에 넣는다. 페이로드 변환(`toMagicUse()` 등)은 인바운드 스레드에서 그대로 해서
      잘못된 요청은 예외로 즉시 거절한다.
- [x] `useMagic` 응답을 큐 액션 안에서 `sessionObject.sendFrameInfo(userId, ...)` 로 보낸다.
      목적지(`/game/{sessionId}/frameInfos/{userId}`)가 동일하므로 클라이언트는 그대로다.
      `InputController` 의 `SimpMessagingTemplate` 필드는 필요 없어져 제거.
- [x] `BotAction.useCard` 를 큐 제출로 교체.
- [x] `SessionObject` 의 핑 콜백 두 개를 큐 제출로 교체(`submitBotToggle`).
- [x] `synchronized` 제거: `MagicInputHandler` 진입점 2개, `PlayerData` 메서드 5개,
      `WordOnlineLoop` 봇 토글 2개.
- [x] `PlayerData.mana` 의 `volatile` 과 `cards` 의 `synchronizedList` 는 유지. #399 에서
      봇이 스냅샷을 읽게 되면 그때 평범한 필드로 내린다. 이유를 주석에 남겼다.
- [x] 테스트: `MagicInputHandlerLockTest` 삭제(모니터 보유를 검증하던 테스트),
      `PlayerDataConcurrencyTest` → `PlayerDataTest` 로 교체(더 이상 참이 아닌 스레드
      안전성 주장 대신 마나/카드 회계를 고정), `GameActionQueueTest` 신규,
      `InputControllerTest` 에 "인바운드 스레드에서 실행하지 않고 큐에 넣는다" 2건 추가.
- [x] `build.gradle` 0.2.0 → 0.2.1 (내부 변경, PATCH).

## Validation

- **Commands to run:** `./gradlew test`
- **Expected output:** 213 tests, 0 failures.

## Risks & Rollback

- **입력 지연:** 프레임 중간에 도착한 캐스팅은 다음 프레임 시작에 실행되므로 최대 한
  프레임(20 FPS 기준 50ms) 늘어난다. 기존에도 입력 스레드는 진행 중인 프레임이 끝날
  때까지 모니터를 기다렸으므로 최악값은 사실상 같고, 대기가 블로킹에서 큐잉으로 바뀐다.
- **큐 포화:** 256개를 넘기면 액션을 버리고 `warn` 을 남긴다. 그 `useMagic` 은 응답도
  가지 않는다. 정상 클라이언트는 프레임당 몇 건이므로 도달하려면 이미 망가진
  클라이언트이고, 무제한 큐로 세션 힙을 키우는 쪽이 더 나쁘다. 새 `InputResultCode` 를
  추가하지 않은 것은 클라이언트 enum 파싱을 깨지 않기 위해서다.
- **액션 실패:** 액션 하나가 던져도 드레인은 계속되고 프레임은 산다. 예전에는 인바운드
  스레드에서 터져 STOMP 에러로 나갔으므로, 이제 실패가 서버 로그로만 보인다.
- **Rollback steps:** `git revert`. 프로토콜·스키마 변경이 없어 서버만 되돌리면 된다.

## Open Questions

- 없음.
