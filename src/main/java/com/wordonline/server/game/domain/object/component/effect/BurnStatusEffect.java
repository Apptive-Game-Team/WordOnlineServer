package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.dto.Effect;


public class BurnStatusEffect extends BaseStatusEffect {
    private float damageTick = 0f;
    private static final float DPS = 1.0f;

    public BurnStatusEffect(GameObject owner, float duration) {
        super(owner, duration);
        gameObject.setEffect(Effect.Burn);
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {
        float dt = gameObject.getGameLoop().deltaTime;
        damageTick += dt;


        Mob mob = gameObject.getComponent(Mob.class);
        while (damageTick >= 1.0f) {
            if (mob != null) {
                mob.onDamaged(new AttackInfo((int)DPS));
            }
            damageTick -= 1.0f;
        }

        super.update();
    }

    @Override
    public void handleAttack(Effect attackEffect) {
        if (attackEffect == Effect.Wet) {
            expire();
        }
    }
}
