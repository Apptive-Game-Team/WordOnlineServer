package com.wordonline.server.game.domain.object.component.mob;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.mob.detector.ClosestEnemyDetector;
import com.wordonline.server.game.domain.object.component.mob.detector.Detector;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.dto.Status;

public class Cannon extends Mob {

    private final float DETECT_INTERVAL = 5;
    private final float ATTACK_RANGE = 5;

    private float timer = 0;
    private final AttackInfo attackInfo;
    private final int targetMask;
    private Detector detector;

    public Cannon(GameObject gameObject, int maxHp, int damage, int targetMask) {
        super(gameObject, maxHp, 0);
        attackInfo = new AttackInfo(damage, ElementType.ROCK);
        this.targetMask = targetMask;
    }

    @Override
    public void onDeath() {
        gameObject.destroy();
    }

    @Override
    public void start() {
        this.detector = new ClosestEnemyDetector(getGameContext(), targetMask);
    }

    @Override
    public void update() {
        super.update();
        timer += getGameContext().getDeltaTime();
        if (timer >= DETECT_INTERVAL) {

            GameObject target = detector.detect(gameObject);

            if (target == null) {
                return;
            }

            double distance = target.getPosition().distance(gameObject.getPosition());

            if (distance > ATTACK_RANGE) {
                return;
            }

            timer = 0;
            getGameContext().getObjectsInfoDtoBuilder()
                            .createProjection(gameObject, target, "RockShot", 0.5f);
            target.getComponent(Mob.class)
                            .onDamaged(attackInfo, 0.5f);
            gameObject.setStatus(Status.Attack);
        }
    }

    @Override
    public void onDestroy() {

    }
}
