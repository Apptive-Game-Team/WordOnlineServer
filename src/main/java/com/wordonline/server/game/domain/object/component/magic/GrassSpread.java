package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;

import java.util.ArrayList;
import java.util.List;

/**
 * Spreads {@link PrefabType#LeafField} around the building it rides on, one field per
 * {@code attackInterval}.
 *
 * <p>The timer is {@link CraterSpawner}'s catch-up shape: a {@code while (elapsed >= interval)}
 * loop, so a frame that ran long still spreads every field it owed instead of dropping them. Each
 * field's position is picked with {@code VineWorldGrowth.spawnRing()}'s
 * {@code cos/sin(angle) * radius} polar placement. Successive fields take the next slot on the
 * current ring, and the ring steps outward once its slots are used up; after the ring at
 * {@code radius} the walk starts over from the inside.
 *
 * <p>{@link #MAX_ALIVE_FIELD_COUNT} bounds how many of this building's leaf fields stand at the
 * same time, not how many it puts down in total. A leaf field carries its own
 * {@code TimedSelfDestroyer(duration)} from {@code AbstractFieldPrefabInitializer}, so each one
 * disappears on its own and stops costing anything: what a frame's {@code overlapSphereAll} and
 * collision pass pays for is the fields still alive. Destroyed fields are dropped from
 * {@link #spreadFields} before every spread, so a field expiring frees a slot and the building
 * keeps spreading for as long as it stands.
 *
 * <p>The cap only binds if a balance pass gives leaf field a {@code duration} much longer than
 * this building's {@code attack_interval}; at {@code duration / attack_interval} fields alive it
 * is inert. VineWorldGrowth already puts 26 collider-bearing vines on the field at once (10 on
 * its inner ring, 16 on its outer) without a reported cost problem, and a grass generator can
 * share the board with other buildings and fields, so the cap sits below that already-shipped
 * simultaneous count rather than at it.
 */
public class GrassSpread extends MagicComponent {

    static final int MAX_ALIVE_FIELD_COUNT = 20;

    // Matches the spacing VineWorldGrowth already uses between its inner and outer vine rings
    // (4f - 2.25f = 1.75f), so a grass generator's rings sit at roughly the same density as
    // ground effects players already see from other buildings.
    private static final float RING_SPACING = 1.75f;

    private final float attackInterval;
    private final float radius;
    private final int quantityPerRing;
    private final int ringCount;
    private final List<GameObject> spreadFields = new ArrayList<>();

    private float elapsed;
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
        spreadFields.removeIf(GameObject::isDestroyed);
        if (spreadFields.size() >= MAX_ALIVE_FIELD_COUNT) {
            // at capacity: keep the slot for the next spread instead of burning it
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

        spreadFields.add(new GameObject(master, PrefabType.LeafField, position, getGameContext()));

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
