package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.physic.Collidable;
import com.wordonline.server.game.dto.Status;

import java.util.List;

@Deprecated
public class Drop extends MagicComponent implements Collidable {
    public static final int SPEED = 2;

    private int direction = 0;
    private final int damage;

    @Override
    public void update() {
        gameObject.setPosition(gameObject.getPosition().plus(0, direction * SPEED * gameObject.getGameLoop().deltaTime, 0));
    }

    public Drop(GameObject gameObject, int damage) {
        super(gameObject);
        direction = -1;
        this.damage = damage;
    }

    @Override
    public void onCollision(GameObject otherObject) {
        List<Damageable> attackables = otherObject.getComponents()
                .stream()
                .filter(component -> component instanceof Damageable)
                .map(component -> (Damageable) component)
                .toList();
        if (attackables.isEmpty()) {
            return;
        }
        direction = 0;
        gameObject.setStatus(Status.Attack);
        otherObject.setStatus(Status.Damaged);
        attackables.forEach(attackable -> attackable.onDamaged(new AttackInfo(damage, gameObject.getElement())));

        gameObject.destroy();
    }
}
