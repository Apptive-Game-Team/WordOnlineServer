package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.BehaviorMob;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.dto.Effect;

public class SnaredStatusEffect extends BaseStatusEffect {
    private static final float SLOW_PERCENT = 0.5f;
    private final int removalDamage;
    private float originalSpeed;

    public SnaredStatusEffect(GameObject owner, float duration, int removalDamage, StatusEffectKey key) {
        super(owner, duration, key);
        this.removalDamage = removalDamage;
        gameObject.setEffect(Effect.Snared);
    }

    @Override
    public void start() {
        Mob mob = gameObject.getComponent(Mob.class);
        if (mob != null) {
            mob.getSpeed().setModifierPercent(-SLOW_PERCENT);
        }

        ZPhysics zp = gameObject.getComponent(ZPhysics.class);
        if (zp != null) {
            zp.LockHover(this);
        }

        BehaviorMob behavior = gameObject.getComponent(BehaviorMob.class);
        if(behavior != null) {
            behavior.getAttackInterval().setModifierPercent(SLOW_PERCENT);
        }
    }

    @Override
    public void onAttacked(ElementType attackType) {
        if (attackType == ElementType.FIRE) {
            Mob mob = gameObject.getComponent(Mob.class);
            if (mob != null) {
                mob.applyDamage(new AttackInfo(removalDamage, ElementType.FIRE));
            }
            expire();
        }
    }

    @Override
    protected void expire() {
        Mob mob = gameObject.getComponent(Mob.class);
        if (mob != null) {
            mob.getSpeed().setModifierPercent(0f);
        }
        ZPhysics zp = gameObject.getComponent(ZPhysics.class);
        if (zp != null) {
            zp.UnlockHover(this);
        }
        BehaviorMob behavior = gameObject.getComponent(BehaviorMob.class);
        if(behavior != null) {
            behavior.getAttackInterval().setModifierPercent(0f);
        }
        super.expire();
    }
}
