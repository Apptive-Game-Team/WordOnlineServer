package com.wordonline.server.game.domain.object.component.mob;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.mob.detector.ClosestEnemyDetector;
import com.wordonline.server.game.domain.object.component.mob.detector.Detector;

public class Cannon extends Mob {

    private final float DETECT_INTERVAL = 5;

    private float timer = 0;
    private final AttackInfo attackInfo;
    private Detector detector;

    public Cannon(GameObject gameObject, int maxHp, int damage) {
        super(gameObject, maxHp, 0);
        attackInfo = new AttackInfo(damage, ElementType.ROCK);
    }

    @Override
    public void onDeath() {

    }

    @Override
    public void start() {
        this.detector = new ClosestEnemyDetector(gameObject.getGameLoop());
    }

    @Override
    public void update() {
        timer += gameObject.getGameLoop().deltaTime;
        if (timer >= DETECT_INTERVAL) {

            GameObject target = detector.detect(gameObject);

            target.getComponents(Damageable.class)
                    .forEach(
                            damageable -> damageable.onDamaged(attackInfo)
                    );

            timer = 0;
        }
    }

    @Override
    public void onDestroy() {

    }
}
