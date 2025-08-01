package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.dto.Effect;

public class SnaredStatusEffect extends BaseStatusEffect {
    private static final float SLOW_PERCENT = -0.5f;
    private final int removalDamage;
    private float originalSpeed;

    public SnaredStatusEffect(GameObject owner, float duration, int removalDamage) {
        super(owner, duration);
        this.removalDamage = removalDamage;
        gameObject.setEffect(Effect.Snared);
    }

    @Override
    public void start() {
        Mob mob = gameObject.getComponent(Mob.class);
        if (mob != null) {
            mob.getSpeed().setModifierPercent(SLOW_PERCENT);
        }
    }

    @Override
    public void handleAttack(ElementType attackType) {
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
        super.expire();
    }
}
