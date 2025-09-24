package com.wordonline.server.game.domain.object.component.mob;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.Stat;
import com.wordonline.server.game.domain.magic.ElementalChart;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.effect.BaseStatusEffect;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Mob extends Component implements Damageable {
    @Getter
    protected int hp;
    @Getter
    protected int maxHp;
    @Getter
    protected Stat speed;

    @Override
    public void onDamaged(AttackInfo attackInfo) {
        gameObject.getComponents(BaseStatusEffect.class)
                .forEach(effect ->
                        attackInfo.getElement().forEach(effect::onAttacked)
                );
        applyDamage(attackInfo);
    }

    public void applyDamage(AttackInfo attackInfo) {
        log.trace("Mob : onDamaged hp: {} damage: {} element: {} ", hp, attackInfo.getDamage(), attackInfo.getElement());
        this.hp -= attackInfo.getDamage() * ElementalChart.computePairwiseProductMultiplier(attackInfo.getElement(),gameObject.getElement().total());
        if (this.hp <= 0) {
            onDeath();
        }
        else if (this.hp > maxHp) {
            this.hp = maxHp;
        }
    }

    public abstract void onDeath();

    public Mob(GameObject gameObject, int maxHp, float speed) {
        super(gameObject);
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.speed = new Stat(speed);
    }
}
