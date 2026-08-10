package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;

public class VineWorldGrowth extends Component {

    private static final float GROWTH_DELAY = 0.75f;
    private static final float INNER_RING_RADIUS = 2.25f;
    private static final float OUTER_RING_RADIUS = 4f;
    private static final int INNER_RING_VINE_COUNT = 10;
    private static final int OUTER_RING_VINE_COUNT = 16;
    private static final float SEED_SPIRIT_SPACING = 0.65f;
    private static final float GOLDEN_ANGLE = 2.3999632f;

    private final VineHitTracker giantVineHitTracker;
    private final VineHitTracker smallVineHitTracker = new VineHitTracker();
    private float elapsed;
    private boolean hasGrown;

    public VineWorldGrowth(GameObject gameObject, VineHitTracker giantVineHitTracker) {
        super(gameObject);
        this.giantVineHitTracker = giantVineHitTracker;
    }

    @Override
    public void start() {
    }

    @Override
    public void update() {
        if (hasGrown) {
            return;
        }

        elapsed += getGameContext().getDeltaTime();
        if (elapsed < GROWTH_DELAY) {
            return;
        }

        spawnRing(INNER_RING_RADIUS, INNER_RING_VINE_COUNT, 0f);
        spawnRing(OUTER_RING_RADIUS, OUTER_RING_VINE_COUNT, (float) Math.PI / OUTER_RING_VINE_COUNT);
        summonSeedSpirits(giantVineHitTracker.hitCount() * 2 + smallVineHitTracker.hitCount());

        hasGrown = true;
        gameObject.removeComponent(this);
    }

    private void spawnRing(float radius, int vineCount, float angleOffset) {
        Vector3 center = gameObject.getPosition();
        Master master = gameObject.getMaster();

        for (int index = 0; index < vineCount; index++) {
            double angle = angleOffset + Math.PI * 2 * index / vineCount;
            Vector3 position = center.plus(
                    (float) Math.cos(angle) * radius,
                    0,
                    (float) Math.sin(angle) * radius
            );
            VineSpawnContext.runWithTracker(
                    smallVineHitTracker,
                    () -> new GameObject(master, PrefabType.Vine, position, getGameContext())
            );
        }
    }

    private void summonSeedSpirits(int summonCount) {
        Vector3 center = gameObject.getPosition();
        Master master = gameObject.getMaster();

        for (int index = 0; index < summonCount; index++) {
            float radius = SEED_SPIRIT_SPACING * (float) Math.sqrt(index + 1);
            float angle = index * GOLDEN_ANGLE;
            Vector3 position = center.plus(
                    (float) Math.cos(angle) * radius,
                    (float) Math.sin(angle) * radius,
                    0
            );
            new GameObject(master, PrefabType.SeedSpirit, position, getGameContext());
        }
    }

    @Override
    public void onDestroy() {
    }
}
