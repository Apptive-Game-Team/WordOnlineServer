package com.wordonline.server.game.domain.object.component.mob;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.mob.detector.ClosestEnemyDetector;
import com.wordonline.server.game.domain.object.component.mob.detector.Detector;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.dto.Status;

import java.util.List;

public class Cannon extends Mob {

    private final static float DEFAULT_ATTACK_DURATION = 0.5f;
    private final static float AOE_RADIUS = 1f;

    private float timer = 0;
    private final AttackInfo attackInfo;
    private final int targetMask;
    private Detector detector;
    private final float attackDuration;
    private final float attackInterval;
    private final float attackRange;

    public Cannon(GameObject gameObject, int maxHp, int damage, int targetMask, float attackInterval, float attackRange) {
        this(gameObject, maxHp, damage, targetMask, DEFAULT_ATTACK_DURATION, attackInterval, attackRange);
    }

    public Cannon(GameObject gameObject, int maxHp, int damage, int targetMask, float attackDuration, float attackInterval, float attackRange) {
        super(gameObject, maxHp, 0);
        attackInfo = new AttackInfo(damage, ElementType.ROCK);
        this.targetMask = targetMask;
        this.attackDuration = attackDuration;
        this.attackInterval = attackInterval;
        this.attackRange = attackRange;
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
        if (timer >= attackInterval) {

            GameObject target = detector.detect(gameObject);

            if (target == null) {
                return;
            }

            double distance = target.getPosition().distance(gameObject.getPosition());

            if (distance > attackRange) {
                return;
            }

            timer = 0;
            getGameContext().getObjectsInfoDtoBuilder()
                            .createProjection(gameObject, target, "RockShot", attackDuration);
            gameObject.setStatus(Status.Attack);

            List<GameObject> victims = getGameContext().overlapSphereAll(target,AOE_RADIUS);
            for (GameObject victim : victims) {
                Mob mob = victim.getComponent(Mob.class);
                if (mob == null) {
                    continue;
                }

                mob.onDamaged(attackInfo, attackDuration);
            }
        }
    }

    @Override
    public void onDestroy() {

    }
}
