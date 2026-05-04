package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.debug.GizmoCategory;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;

public class OvergrowthExplosion extends MagicComponent {
    private static final float EFFECT_DELAY = 0.5f;
    private static final float SUMMON_SPACING = 0.5f;

    private final float effectRadius;
    private final int summonCount;
    private float counter = 0f;
    private boolean isRunning = true;

    public OvergrowthExplosion(GameObject gameObject, float effectRadius, int summonCount) {
        super(gameObject);
        this.effectRadius = effectRadius;
        this.summonCount = Math.max(1, summonCount);
    }

    @Override
    public void start() {
        gameObject.drawCircle(Vector3.ZERO, effectRadius, GizmoCategory.AreaOfEffect);
    }

    @Override
    public void update() {
        if (!isRunning) {
            return;
        }

        if (counter < EFFECT_DELAY) {
            counter += getGameContext().getDeltaTime();
            return;
        }

        Master owner = gameObject.getMaster();
        int transformedCount = 0;

        for (GameObject target : getGameContext().overlapSphereAll(gameObject, effectRadius)) {
            if (target == gameObject || target.isDestroyed()) {
                continue;
            }
            if (owner != Master.None && target.getMaster() != owner) {
                continue;
            }
            if (target.getType() != PrefabType.SeedSpirit) {
                continue;
            }

            Vector3 targetPosition = new Vector3(target.getPosition());
            Master targetMaster = target.getMaster();
            target.destroy();
            new GameObject(targetMaster, PrefabType.TreeGolem, targetPosition, getGameContext());
            transformedCount++;
        }

        if (transformedCount == 0) {
            summonSeedSpirits(owner);
        }

        isRunning = false;
        gameObject.destroy();
    }

    private void summonSeedSpirits(Master owner) {
        Vector3 basePosition = gameObject.getPosition();
        float centerOffset = (summonCount - 1) / 2f;

        for (int i = 0; i < summonCount; i++) {
            float offsetX = (i - centerOffset) * SUMMON_SPACING;
            Vector3 summonPosition = basePosition.plus(offsetX, 0, 0);
            new GameObject(owner, PrefabType.SeedSpirit, summonPosition, getGameContext());
        }
    }
}
