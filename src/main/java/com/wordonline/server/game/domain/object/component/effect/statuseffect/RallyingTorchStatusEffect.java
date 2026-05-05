package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.BehaviorMob;

public class RallyingTorchStatusEffect extends BaseStatusEffect {

    private final float buffPercent;
    private boolean speedApplied;
    private boolean attackIntervalApplied;

    public RallyingTorchStatusEffect(GameObject owner, float duration, float buffPercent, StatusEffectKey key) {
        super(owner, duration, key);
        this.buffPercent = buffPercent;
    }

    @Override
    public void start() {
        Mob mob = gameObject.getComponent(Mob.class);
        if (mob != null && !speedApplied) {
            mob.getSpeed().addPercent(buffPercent);
            speedApplied = true;
        }

        BehaviorMob behaviorMob = gameObject.getComponent(BehaviorMob.class);
        if (behaviorMob != null && !attackIntervalApplied) {
            behaviorMob.getAttackInterval().addPercent(-buffPercent);
            attackIntervalApplied = true;
        }
    }

    @Override
    public void onAttacked(ElementType attackType) {
    }

    @Override
    protected void expire() {
        removeBuff();
        gameObject.removeComponent(this);
    }

    @Override
    public void onDestroy() {
        removeBuff();
    }

    private void removeBuff() {
        if (speedApplied) {
            Mob mob = gameObject.getComponent(Mob.class);
            if (mob != null) {
                mob.getSpeed().addPercent(-buffPercent);
            }
            speedApplied = false;
        }

        if (attackIntervalApplied) {
            BehaviorMob behaviorMob = gameObject.getComponent(BehaviorMob.class);
            if (behaviorMob != null) {
                behaviorMob.getAttackInterval().addPercent(buffPercent);
            }
            attackIntervalApplied = false;
        }
    }
}
