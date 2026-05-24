# Client Prompt: Fire Lord Spirit and Dimension Toad

Use this prompt for the client-side implementation of issue #261.

## Context

The server adds two new Spawn magic recipes and five new prefabs:

- `fire_lord_spirit`: `Wind + Fire + Spawn + Spawn`
- `dimension_toad`: `Fire + Lightning + Spawn + Spawn`
- `FireLordSpirit`
- `FireChildSpirit`
- `DimensionToad`
- `FireTadpole`
- `LightningTadpole`

## Prompt

Implement client-side support for the new server magic and prefabs from issue #261.

Requirements:

1. Add visual prefab mappings for `FireLordSpirit`, `FireChildSpirit`, `DimensionToad`, `FireTadpole`, and `LightningTadpole`.
2. Add user-facing magic metadata for `fire_lord_spirit` and `dimension_toad`.
3. Support duplicate-card recipes:
   - Fire Lord Spirit requires `Wind`, `Fire`, `Spawn`, `Spawn`.
   - Dimension Toad requires `Fire`, `Lightning`, `Spawn`, `Spawn`.
4. Make Fire Lord Spirit read as a large boss-like fire/wind summon.
5. Make Fire Child Spirit read as a small fire summon that attacks with ember/fireball projectiles.
6. Make Dimension Toad read as a fire/lightning cowardly summoner.
7. Make Fire Tadpole and Lightning Tadpole small, fast coward units with distinct fire and lightning visuals.
8. Add projectile or hit VFX for Fire Child Spirit using the existing server projectile type `FireShot`.
9. Reuse existing client animation/state handling for idle, move, attack, damaged, and destroyed states.
10. Verify all five prefabs can be spawned from frame creation events without missing prefab/type errors.

Server behavior to mirror visually:

- Fire Lord Spirit summons one Fire Child Spirit per second, capped at five total summons.
- Dimension Toad alternates Fire Tadpole and Lightning Tadpole summons once per second.
- Dimension Toad and both tadpoles use coward behavior, so their movement should look skittish or evasive when possible.

Validation:

- Cast `Wind + Fire + Spawn + Spawn` and confirm Fire Lord Spirit appears and child spirits spawn over time.
- Cast `Fire + Lightning + Spawn + Spawn` and confirm Dimension Toad appears and tadpoles spawn over time.
- Confirm duplicate `Spawn` cards are shown and consumed correctly in recipe UI.
- Confirm no unknown prefab, unknown magic, or missing asset warnings appear.
