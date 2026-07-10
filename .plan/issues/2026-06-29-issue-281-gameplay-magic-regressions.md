# 2026-06-29 — Gameplay magic behavior regressions

- Date: 2026-06-29
- GitHub Issue: #281
- Status: Draft

## Goal

Fix reported gameplay regressions for Towerback, Tide Call, Chain Lightning, Lightning Drop, Wind Totem, and coward fleeing behavior, with deterministic tests or focused reproduction notes for each behavior.

## Non-goals

- Do not make broad balance tuning without tying each value change to a verified behavior bug.
- Do not add production SQL under `src/main/resources`; shared gameplay data belongs in `../database/migration`.
- Do not rewrite the combat, physics, or prefab systems while fixing these regressions.

## Context / Constraints

- Tide Call currently fails during prefab initialization with `Parameter not found: tide_call, radius`.
- `TideCallPrefabInitializer` reads `radius`, `damage`, `speed`, and `duration` from `GameObjectKey.TIDE_CALL`.
- `CowardMob` previously set `hasPanicked = true` after panic ended, then all normal states skipped future threat detection while that flag remained true.
- Intended coward behavior is panic for the DB-backed `panic_duration`, then ignore new threats for a fixed 10 second cooldown, then allow panic again.
- Dimension Toad uses `NonAttackingCowardMob`, so coward lifecycle bugs directly affect the reported Dimension Toad behavior.
- `WindPushComponent` currently pushes only enemy mobs and explicitly skips same-master targets.
- Towerback combines ground `AttackMob` behavior with air `Tower` behavior on one object; ground behavior must be isolated before changing shared tower logic.

## Approach (Checklist)

- [x] **Step 0: Recon** (Inspect existing code, DB migrations, and likely fault lines)
  - Checked `CowardMob`, `NonAttackingCowardMob`, and `DimensionToadPrefabInitializer`.
  - Checked Tide Call, Chain Lightning, Lightning Drop, Wind Totem, and Towerback prefab/component code.
  - Checked current migration source for related game object parameters.
- [ ] **Step 1: Reproduction / Tests**
  - Add or identify focused tests for missing Tide Call parameters.
  - Add a coward repeat-threat test or component-level simulation proving repeated flee behavior.
  - Add focused Wind Totem target filtering coverage.
  - Reproduce Towerback ground attack separately from its air tower attack.
  - Reproduce Chain Lightning delay/movement behavior before tuning constants.
  - Reproduce Lightning Drop failure path before changing AoE/lifetime logic.
- [x] **Step 2: Implementation**
  - Added missing Tide Call parameter seed migration in `../database/migration` and synced test fixtures.
  - Adjusted coward threat categories and panic lifecycle so Dimension Toad can flee immediately, run for `panic_duration`, wait 10 seconds, then flee again.
  - Updated Wind Totem push filtering to include allied mobs while still excluding self and non-mob targets.
  - Split Towerback ground attack onto `sub_attack_range` so its ground attack is not using the air tower range.
  - Reduced Chain Lightning chain delay while preserving chain handoff behavior.
  - Grounded Lightning Drop visual object before strike evaluation so drop placement and AoE line up.
- [ ] **Step 3: Validation**
  - Run `./gradlew test`.
  - Run targeted tests for changed components/prefabs if available.
  - Verify migration/test fixture consistency for DB-backed parameters.
- [ ] **Step 4: Rollout / Rollback**
  - Roll out as one issue branch because reports are related gameplay regressions.
  - Keep commits separable by subsystem if fixes become independent.
  - Roll back by reverting the specific component/migration commits; no feature flag expected.

## Architecture Decisions

- Treat DB-backed gameplay values as data-layer changes in the database repository path, not as hardcoded prefab defaults.
- Keep fixes local to existing components (`CowardMob`, `WindPushComponent`, `ChainShot`, `LightningStrike`, `TowerbackPrefabInitializer`) unless tests prove a lower-level engine bug.
- Prefer behavior tests over broad integration setup where possible because these bugs are component-specific and easy to overfit with manual tuning.

## Risks & Tradeoffs

- Shared component changes can affect more prefabs than the reported spells; tests should cover representative users.
- Towerback has two attack components on one object, so changing shared `Tower` or `AttackMob` behavior may cause unrelated regressions.
- Chain Lightning may need balance values after logic is fixed; keep that separate from deterministic bug fixes.
- Tide Call migration must be published before server code depending on the parameter set is deployed.

## Open Questions

- What exact behavior is meant by "lightning drop 삐꾸" if reproduction does not reveal an obvious failure?
- For Wind Totem, should allied buildings be pushed too, or only allied units/mobs?
