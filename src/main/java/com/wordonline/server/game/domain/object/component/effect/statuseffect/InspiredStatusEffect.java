package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.mob.simple.TimedBehaviorMob;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.BehaviorMob;
import com.wordonline.server.game.dto.Effect;

public class InspiredStatusEffect extends BaseStatusEffect {

    private static final float ATTACK_SPEED_BONUS_PERCENT = 0.5f;
    private static final float ATTACK_INTERVAL_MODIFIER =
            -ATTACK_SPEED_BONUS_PERCENT / (1f + ATTACK_SPEED_BONUS_PERCENT);

    public InspiredStatusEffect(GameObject owner, float duration, StatusEffectKey key) {
        super(owner, duration, key);
    }

    @Override
    public void start() {
        BehaviorMob behaviorMob = gameObject.getComponent(BehaviorMob.class);
        if (behaviorMob != null) {
            behaviorMob.getAttackInterval().setModifierPercent(ATTACK_INTERVAL_MODIFIER);
        }

        TimedBehaviorMob timedBehaviorMob = gameObject.getComponent(TimedBehaviorMob.class);
        if (timedBehaviorMob != null) {
            timedBehaviorMob.getAttackInterval().setModifierPercent(ATTACK_INTERVAL_MODIFIER);
        }

        gameObject.setEffect(Effect.Inspired);
    }

    @Override
    public void onAttacked(ElementType attackType) {
    }

    @Override
    protected void expire() {
        BehaviorMob behaviorMob = gameObject.getComponent(BehaviorMob.class);
        if (behaviorMob != null) {
            behaviorMob.getAttackInterval().setModifierPercent(0f);
        }

        TimedBehaviorMob timedBehaviorMob = gameObject.getComponent(TimedBehaviorMob.class);
        if (timedBehaviorMob != null) {
            timedBehaviorMob.getAttackInterval().setModifierPercent(0f);
        }

        super.expire();
    }
}
