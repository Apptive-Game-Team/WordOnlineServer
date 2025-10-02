package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DOTStatusEffect extends BaseStatusEffect {
    private float curTick = 0f;
    private final float tickInterval;   // > 0
    private final int unit;             // +1 = dmg, -1 = heal
    private final ElementType elementType;

    public DOTStatusEffect(GameObject owner, float duration, int totalAmount, ElementType elementType, StatusEffectKey key) {
        super(owner, duration, key);
        this.elementType = elementType;

        int totalUnits = Math.abs(totalAmount); // 적용 횟수
        this.tickInterval = initialDuration / totalUnits;
        this.unit = (totalAmount >= 0f) ? +1 : -1;
    }

    @Override public void start() {}

    @Override
    public void update() {
        super.update();
        curTick += gameObject.getGameLoop().deltaTime;
        int ticks = (int)Math.floor(curTick / tickInterval);
        if (ticks <= 0) return;
        curTick -= ticks * tickInterval;

        Mob mob = gameObject.getComponent(Mob.class);
        if (mob == null) return;
        mob.applyDamage(new AttackInfo(unit * ticks, elementType));
    }

    @Override public void onAttacked(ElementType attackType) {}
    @Override public void expire() {
        super.expire();
    }
}
