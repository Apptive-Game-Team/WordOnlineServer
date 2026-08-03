package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.mob.detector.ClosestEnemyDetector;
import com.wordonline.server.game.domain.object.component.mob.detector.Detector;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;

public class OverchargeStatusEffect extends BaseStatusEffect {

    private static final float SPEED_BONUS_PERCENT = 0.5f;
    private static final float SHOT_INTERVAL = 1f;
    private static final float SHOT_DURATION = 0.2f;
    // ponytail: 전기 타워 파라미터는 DB(다른 레포) 소관이라 상수 고정. 밸런스 조정 필요하면 electric_tower 파라미터로 이관.
    private static final int SHOT_DAMAGE = 3;
    private static final float SHOT_RANGE = 3f;

    private float shotCooldown = SHOT_INTERVAL;
    private Detector detector;

    public OverchargeStatusEffect(GameObject owner, float duration, StatusEffectKey key) {
        super(owner, duration, key);
    }

    @Override
    public void start() {
        Mob mob = gameObject.getComponent(Mob.class);
        if (mob != null) {
            mob.getSpeed().setModifierPercent(SPEED_BONUS_PERCENT);
        }
    }

    @Override
    public void update() {
        shotCooldown -= getGameContext().getDeltaTime();
        if (shotCooldown <= 0f) {
            shotCooldown = SHOT_INTERVAL;
            shoot();
        }
        super.update();
    }

    private void shoot() {
        if (detector == null) {
            detector = new ClosestEnemyDetector(getGameContext(), TargetMask.ANY.bit);
        }

        GameObject target = detector.detect(gameObject);
        if (target == null || target.getPosition().distance(gameObject.getPosition()) > SHOT_RANGE) {
            return;
        }

        getGameContext().getObjectsInfoDtoBuilder()
                .createProjection(gameObject, target, "ElectricShot", SHOT_DURATION);
        AttackInfo attackInfo = new AttackInfo(SHOT_DAMAGE, ElementType.LIGHTNING);
        target.getComponents(Damageable.class)
                .forEach(damageable -> damageable.onDamaged(attackInfo, SHOT_DURATION));
    }

    @Override
    public void onAttacked(ElementType attackType) {
    }

    @Override
    protected void expire() {
        Mob mob = gameObject.getComponent(Mob.class);
        if (mob != null) {
            mob.getSpeed().setModifierPercent(0f);
        }
        super.expire();
    }
}
