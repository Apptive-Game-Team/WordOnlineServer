package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.BehaviorMob;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.dto.Effect;

public class StunStatusEffect extends BaseStatusEffect {

    private final float duration;
    private final Effect effect;
    private final float gravityMultiplier;

    private ZPhysics zPhysics;
    private float originalGravity;
    private boolean gravityApplied;

    public StunStatusEffect(GameObject owner, float duration, StatusEffectKey key, Effect effect) {
        this(owner, duration, key, effect, 1f);
    }

    public StunStatusEffect(GameObject owner, float duration, StatusEffectKey key, Effect effect, float gravityMultiplier) {
        super(owner, duration, key, effect);
        this.duration = duration;
        this.effect = effect;
        this.gravityMultiplier = gravityMultiplier;
    }

    @Override
    public void start() {
        zPhysics = gameObject.getComponent(ZPhysics.class);
        BehaviorMob behavior = gameObject.getComponent(BehaviorMob.class);

        if (zPhysics != null) {
            zPhysics.lockHover(this);
            applyGravityMultiplier();
        }
        if (behavior != null) {
            behavior.setStun(duration);
        }
    }

    private void applyGravityMultiplier() {
        if (gravityApplied || gravityMultiplier == 1f) {
            return;
        }

        originalGravity = zPhysics.getGravity();
        zPhysics.setGravity(originalGravity * gravityMultiplier);
        gravityApplied = true;
    }

    @Override
    public void onAttacked(ElementType attackType) {
    }

    @Override
    protected void expire() {
        if (gravityApplied && zPhysics != null) {
            zPhysics.setGravity(originalGravity);
            gravityApplied = false;
        }
        if (zPhysics != null) {
            zPhysics.unlockHover(this);
        }

        BehaviorMob behavior = gameObject.getComponent(BehaviorMob.class);
        if (behavior != null) {
            behavior.setIdle();
        }
        super.expire();
    }
}
