package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;

/**
 * Spreads {@link PrefabType#LeafField} around the building it rides on, one field per
 * {@code attackInterval}.
 *
 * <p>The timer is {@link CraterSpawner}'s catch-up shape: a {@code while (elapsed >= interval)}
 * loop, so a frame that ran long still spreads every field it owed instead of dropping them. Each
 * field's position is picked with {@code VineWorldGrowth.spawnRing()}'s
 * {@code cos/sin(angle) * radius} polar placement. The inner ring fills one slot at a time;
 * once a ring reaches {@code radius} the next field starts the ring sequence over from the
 * inside, so the building keeps spreading fields in growing rings for as long as it stands.
 *
 * <p>{@link #MAX_SPAWN_COUNT} bounds how many fields one grass generator ever puts down over its
 * whole lifetime, independent of whatever {@code radius} / {@code attack_interval} /
 * {@code quantity} / field {@code duration} a balance pass ends up choosing: every field this
 * spreads adds a trigger collider that every frame's {@code overlapSphereAll} and collision pass
 * has to consider, so the count one building can put on the field must be bounded in code rather
 * than trusted to whatever those parameters turn out to be. VineWorldGrowth already puts 26
 * collider-bearing vines on the field in a single cast (10 on its inner ring, 16 on its outer)
 * without a reported cost problem; a grass generator can share the board with other buildings and
 * fields at the same time, so its lifetime total is kept below that already-shipped burst rather
 * than at it.
 */
public class GrassSpread extends MagicComponent {

    static final int MAX_SPAWN_COUNT = 20;

    // Matches the spacing VineWorldGrowth already uses between its inner and outer vine rings
    // (4f - 2.25f = 1.75f), so a grass generator's rings sit at roughly the same density as
    // ground effects players already see from other buildings.
    private static final float RING_SPACING = 1.75f;

    private final float attackInterval;
    private final float radius;
    private final int quantityPerRing;
    private final int ringCount;

    private float elapsed;
    private int spawnedCount;
    private int ringIndex;
    private int slotIndex;

    public GrassSpread(GameObject gameObject, float attackInterval, float radius, int quantityPerRing) {
        super(gameObject);
        this.attackInterval = Math.max(0.05f, attackInterval);
        this.radius = Math.max(0f, radius);
        this.quantityPerRing = Math.max(1, quantityPerRing);
        this.ringCount = Math.max(1, (int) Math.ceil(this.radius / RING_SPACING));
    }

    @Override
    public void update() {
        if (gameObject.isDestroyed()) {
            return;
        }

        elapsed += getGameContext().getDeltaTime();
        while (elapsed >= attackInterval) {
            elapsed -= attackInterval;
            spreadOne();
        }
    }

    private void spreadOne() {
        if (spawnedCount >= MAX_SPAWN_COUNT) {
            return;
        }

        float ringRadius = (ringIndex == ringCount - 1) ? radius : RING_SPACING * (ringIndex + 1);
        float angleOffset = (ringIndex % 2 == 0) ? 0f : (float) Math.PI / quantityPerRing;
        double angle = angleOffset + Math.PI * 2 * slotIndex / quantityPerRing;

        Vector3 center = gameObject.getPosition();
        Master master = gameObject.getMaster();
        Vector3 position = center.plus(
                (float) Math.cos(angle) * ringRadius,
                0,
                (float) Math.sin(angle) * ringRadius
        );

        new GameObject(master, PrefabType.LeafField, position, getGameContext());
        spawnedCount++;

        slotIndex++;
        if (slotIndex >= quantityPerRing) {
            slotIndex = 0;
            ringIndex++;
            if (ringIndex >= ringCount) {
                // reached radius: start over from the inside instead of stopping
                ringIndex = 0;
            }
        }
    }
}
