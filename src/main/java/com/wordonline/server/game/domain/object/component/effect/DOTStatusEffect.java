package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.dto.Effect;


public class DOTStatusEffect extends BaseStatusEffect {
    private float damageTick = 0f;
    private final float applyTick; // duration / damage -> 1데미지 주는데 걸린 시간
    private final float totalDamage;
    private final ElementType elementType;
    private static final int UNIT_DAMAGE = 1; // 단위 데미지

    public DOTStatusEffect(GameObject owner, float duration, float totalDamage, ElementType elementType) {
        super(owner, duration);
        this.totalDamage = totalDamage;
        applyTick = (initialDuration / totalDamage) * UNIT_DAMAGE;
        this.elementType = elementType;
    }

    @Override
    public void start() {}

    @Override
    public void update() {
        float dt = gameObject.getGameLoop().deltaTime;
        damageTick += dt;

        Mob mob = gameObject.getComponent(Mob.class);
        if (damageTick >= applyTick) {
            if (mob != null) {
                mob.applyDamage(new AttackInfo((UNIT_DAMAGE * (int)damageTick),elementType));
            }
            damageTick -= applyTick;
        }

        super.update();
    }

    @Override
    public void onAttacked(ElementType attackType) {}

    @Override
    public void expire()
    {

    }
}
