# 2026-08-16 — 봇에게 라이브 객체 대신 불변 프레임 스냅샷 주기

- Date: 2026-08-16
- GitHub Issue: #399 (#398 후속, PR #402 위에 스택)
- Status: Done

## Goal

봇 executor 스레드가 게임 상태를 **읽는** 경로를 없앤다. 루프 스레드가 프레임에서
불변 스냅샷을 떠서 넘기고, 봇은 그것만 본다. 그 결과로 읽기 방어용 동시성 장치
(`CopyOnWriteArrayList`, `BotEye`/`BotAgent` 의 `synchronized`, `PlayerData` 의
`volatile`/`synchronizedList`)를 모두 걷어낸다.

## Non-goals

- 봇 판단 로직 변경. 점수 계산, 후보 선정, 사거리 판정은 그대로다.
- 봇 사고를 루프 스레드로 옮기는 것. 여전히 별도 executor 에서 돌린다. 프레임 예산
  50ms 안에 넣을 이유가 없다.
- `SessionService.createSession`, `ServerStatusService` 의 `synchronized`.

## Context / Constraints

- #398 이 **쓰기**를 루프 스레드로 모았다. 읽기는 남아 있었다.
- `BotEye` 는 `data.gameObjects` 를 복사했지만 **리스트만** 복사했다. 원소인
  `GameObject` 는 `PhysicSystem` / `ComponentUpdateSystem` 이 매 프레임 바꾸는 라이브
  객체 그대로라, 봇은 찢어진 상태를 읽었다. COW 리스트는 이걸 막지 못한다.
- `Vector3` 는 `@Data` 라 mutable 이다. 위치는 값 복사가 필요하다.
- `BotBrain` 이 `GameObject` 에서 읽는 것은 셋뿐이다: `getMaster()`, `getPosition()`,
  그리고 `BotCounterEvaluator` 를 통한 `getType()`.
- `BotAgentSystem` 은 이미 같은 패턴을 쓰고 있었다. `FrameInfoDto` 를 루프 스레드에서
  뽑아 executor 에 넘긴다. 다만 그 값은 `BotEye` 생성자에서 무시되고 있었다.
- `BotEye` 밖에서 `gameObjects` 를 읽는 코드는 전부 루프 스레드 컴포넌트다
  (`SimplePhysics`, 각종 detector, `RallyingTotem`, `AreaEffectProvider` …).
- `PlayerData.cards` 를 오프루프에서 만지는 곳이 하나 더 있었다. 디버그 HTTP 엔드포인트가
  타는 `SessionObject.setLeftUser` / `setRightUser` 다.

## Approach (Checklist)

- [x] `BotVisibleObject(master, type, position)` 레코드 추가. `position` 은 `Vector3`
      복사본이다.
- [x] `BotEye` 를 불변 레코드로 바꾸고 `observe(GameSessionData, Master)` 정적 팩토리를
      둔다. 쓰이지 않던 `FrameInfoDto` 파라미터 제거.
- [x] `BotAgentSystem` 이 루프 스레드에서 `BotEye.observe(...)` 로 스냅샷을 떠서
      `botAgent.onTick(botEye)` 에 넘긴다. `FrameInfoDto` 공급자 제거.
- [x] `BotBrain.think(BotEye, Parameters, Master)` 로 시그니처 변경. `GameLoop` 의존과
      `List<GameObject>` 의존이 사라진다. `Parameters` 는 세션 시작에 로드되고 이후
      읽기 전용이다.
- [x] `BotCounterEvaluator.evaluate` 가 `List<BotVisibleObject>` 를 받는다.
- [x] `GameSessionData.gameObjects`: `CopyOnWriteArrayList` → `ArrayList`.
- [x] `PlayerData`: `volatile int mana` → `int`, `synchronizedList` → `ArrayList`.
- [x] `BotAgent`: `shouldProcess` / `onTick` 의 `synchronized` 제거. `pendingDecision` 은
      `volatile` 로 내리고, `hasReadyPendingDecision` 은 필드를 한 번만 읽는다.
- [x] `SessionObject.setLeftUser` / `setRightUser` 의 손패·덱 초기화를 액션 큐로 넘긴다.
      `leftUserId` / `rightUserId` 대입은 호출자가 즉시 되읽으므로 인라인 유지.
- [x] 테스트: `GameSessionDataConcurrencyTest` 삭제(COW 속성을 고정하던 테스트),
      `BotEyeTest` 신규.
- [x] `build.gradle` 0.2.1 → 0.2.2 (내부 변경, PATCH).

## Validation

- **Commands to run:** `./gradlew test`
- **Expected output:** 217 tests, 0 failures.
- 봇 동작 회귀는 `BotGameSchedulerTest`, `BotPersonaServiceTest` 와 기존 스위트로 확인.
- `grep -rn synchronized src/main/java/com/wordonline/server/game` → 0건.

## Risks & Rollback

- **`pendingDecision` 의 남은 경합:** 루프 스레드(`shouldProcess`)가 읽고 봇
  스레드(`onTick`)가 읽고 쓴다. `BotAgentSystem` 의 CAS 가 `onTick` 동시 실행을 막으므로
  쓰기는 한 스레드뿐이고, `volatile` 로 가시성만 보장하면 된다. 낡은 값을 읽으면 최악이
  즉시 반환하는 tick 한 번 또는 반응 주기 한 번 스킵이다. 게임에 보이지 않는다.
- **스냅샷 비용:** 프레임당 객체 수만큼 레코드를 만든다. 봇이 실제로 tick 하는 프레임에만
  뜨고(반응 주기마다), COW 리스트가 매 프레임 물던 배열 복사가 사라지므로 순증이 아니다.
- **디버그 덱 설정 지연:** `setLeftUser` 의 덱 초기화가 최대 한 프레임 늦게 반영된다.
  디버그 전용 경로다.
- **Rollback steps:** `git revert`. 프로토콜·스키마 변경 없음.

## Open Questions

- 없음.
