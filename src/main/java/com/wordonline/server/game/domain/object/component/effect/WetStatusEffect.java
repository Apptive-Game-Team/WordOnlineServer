package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.dto.Effect;

public class WetStatusEffect extends BaseStatusEffect {

    public final DOTStatusEffect dotHeal;
    private static final float TOTAL_HEAL = 5;

    public WetStatusEffect(GameObject owner, float duration) {
        super(owner, duration);
        gameObject.setEffect(Effect.Wet);
        dotHeal = new DOTStatusEffect(owner, duration, -TOTAL_HEAL, ElementType.NONE);
    }

    @Override
    public void onAttacked(ElementType attackType) {}

    @Override
    public void start() {
        gameObject.addTempElement(ElementType.WATER, this);
        gameObject.addComponent(dotHeal);

        BurnStatusEffect burnSE = gameObject.getComponent(BurnStatusEffect.class);
        if(burnSE != null)
        {
            burnSE.expire();
            expire();
        }
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void expire() {
        gameObject.removeTempElement(ElementType.WATER, this);
        if(gameObject.getComponents().contains(dotHeal)) gameObject.removeComponent(dotHeal);
        super.expire();
    }
}
