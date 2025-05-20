package com.wordonline.server.game.domain.object.component.mob;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Attackable;
import com.wordonline.server.game.domain.object.component.Component;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Mob extends Component implements Attackable {
    protected int hp;
    protected int maxHp;
    @Getter
    protected int speed;

    @Override
    public void onAttack(int damage) {
        log.info("Mob : onAttack hp: {} damage: {}", hp, damage);
        this.hp -= damage;
        if (this.hp <= 0) {
            onDeath();
        }
    }

    public abstract void onDeath();

    public Mob(GameObject gameObject, int maxHp, int speed) {
        super(gameObject);
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.speed = speed;
    }
}
