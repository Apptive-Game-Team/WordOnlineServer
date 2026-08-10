# 2026-08-06 — 프레임 이벤트 리스트 보완 (타격 이벤트)

- Date: 2026-08-06
- GitHub Issue: #356 (PR #357)
- Status: Draft

## Goal

이미 올라와 있는 PR #357 의 이벤트 리스트 위에, 클라이언트 타격 이펙트
(Apptive-Game-Team/WordOnlineClient#449) 가 실제로 쓸 수 있을 만큼 커버리지를 채운다.

## Non-goals

- `hit` 외 이벤트 종류 추가.
- 지속 피해(도트), 낙하 피해, 힐처럼 지목할 공격자가 없는 피해의 이벤트화.
- PR #357 이 정한 이벤트 표현(`GameEventDto` 레코드 + `type` 판별자) 변경.

## Context / Constraints

- PR #357 이 `GameEventDto`, `GameContext` 버퍼와 drain, `FrameInfoDto.events`,
  `Mob.applyDamage` 발행, 그리고 `AttackMob` 한 곳의 `attackerId` 부착까지 끝냈다.
- 남은 구멍 세 가지:
  1. `SyncFrameDataSystem` 이 10프레임마다 `frame` 대신 `sync` 를 보내는데
     `SyncInfoDto` 에 events 가 없어 그 프레임의 타격이 유실된다.
  2. `attackerId` 를 다는 곳이 근접 공격 하나뿐이라 마법·투사체·타워 피해에는
     공격자가 없다.
  3. 발행 조건과 전송 형태를 고정하는 테스트가 없다.
- 투사체는 두 종류다. `Shot` 계열 마법은 실제 `GameObject` 라 자기 id 를 쓰고,
  `ProjectileRangeAttackMob` 등의 투사체는 `createProjection` 시각 효과일 뿐이라
  데미지는 쏜 유닛이 직접 가한다. 둘 다 `gameObject` 가 곧 공격자다.

## Approach (Checklist)

- [x] `AttackInfo.withAttacker(GameObject)` — 인라인 표현식이 대부분이라 세터보다 낫다.
      `AttackMob` 도 이 형태로 정리.
- [x] 공격자가 분명한 27개 콜사이트에 부착 (근접·원거리·타워·자폭·마법).
      낙하 피해(`ZPhysics`), 도트/속박/과충전, 소환진 자해, 힐은 제외.
- [x] `SyncInfoDto` 가 대체하는 프레임의 events 를 그대로 싣는다.
- [x] 테스트: `MobHitEventTest` (발행 조건), `FrameEventPayloadTest` (전송 형태와 sync 전달).

## Validation

- **Commands to run:** `./gradlew test`
- **Expected output:** 108 tests, 0 failures.

## Risks & Rollback

- **Risks:** 프레임당 이벤트 수가 늘어 페이로드가 커진다. 실제 피해가 들어간 타격만
  발행하므로 상한은 낮다.
- **Rollback steps:** `git revert`. 클라이언트는 모르는 필드를 무시하므로 서버만 되돌려도 된다.

## Open Questions

- 없음.
