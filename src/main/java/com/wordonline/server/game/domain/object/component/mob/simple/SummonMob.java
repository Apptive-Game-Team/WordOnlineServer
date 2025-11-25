package com.wordonline.server.game.domain.object.component.mob.simple;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.mob.detector.ClosestEnemyDetector;
import com.wordonline.server.game.domain.object.component.mob.detector.Detector;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

public class SummonMob extends TimedBehaviorMob {

    private final int targetMask = TargetMask.GROUND.bit;
    private float attackRange;
    private PrefabType prefabType;
    private Detector detector;

    private final Behavior behavior = () -> {
        GameObject target = detector.detect(gameObject);

        if (target == null) {
            return false;
        }

        Vector3 position = target.getPosition();

        if (gameObject.getPosition().distance(position) > attackRange) {
            return false;
        }

        new GameObject(
                gameObject.getMaster(),
                prefabType,
                position,
                getGameContext()
        );
        return true;
    };

    public SummonMob(GameObject gameObject, int maxHp,
            float speed, float interval, float attackRange, PrefabType prefabType) {
        super(gameObject, maxHp, speed, interval, null);
        setBehavior(behavior);
        this.prefabType = prefabType;
        this.attackRange = attackRange;
    }

    @Override
    public void onDeath() {

    }

    @Override
    public void start() {
        this.detector = new ClosestEnemyDetector(getGameContext(), targetMask);
    }

    @Override
    public void onDestroy() {

    }
}
