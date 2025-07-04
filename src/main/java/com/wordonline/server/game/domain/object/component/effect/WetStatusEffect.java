package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.dto.Effect;

public class WetStatusEffect extends BaseStatusEffect {

    public WetStatusEffect(GameObject owner, float duration) {
        super(owner, duration);
        gameObject.setEffect(Effect.Wet);
    }

    @Override
    public void handleAttack(ElementType attackType) {
        Mob mob = gameObject.getComponent(Mob.class);
        if (mob == null) return;

        switch (attackType) {
            //Not Implemented ElementType...
//            case Fire -> {
//                mob.setDamageMultiplier(0.5f);
//                expire();
//            }
//            case Lightning -> {
//                mob.setDamageMultiplier(2.0f);
//                expire();
//            }
            default -> {

            }
        }
    }

    @Override
    public void start() {

    }
}
