package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Attackable;
import com.wordonline.server.game.domain.object.component.Collidable;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameLoop;

import java.util.List;

public class Drop extends MagicComponent implements Collidable {
    public static final int SPEED = 2;

    private int direction = 0;
    private final int damage;

    @Override
    public void update() {
        gameObject.setPosition(gameObject.getPosition().add(0, direction * SPEED * gameObject.getGameLoop().deltaTime));
    }

    public Drop(GameObject gameObject, int damage) {
        super(gameObject);
        direction = -1;
        this.damage = damage;
    }

    @Override
    public void onCollision(GameObject otherObject) {
        List<Attackable> attackables = otherObject.getComponents()
                .stream()
                .filter(component -> component instanceof Attackable)
                .map(component -> (Attackable) component)
                .toList();
        if (attackables.isEmpty()) {
            return;
        }
        direction = 0;
        gameObject.setStatus(Status.Attack);
        otherObject.setStatus(Status.Damaged);
        attackables.forEach(attackable -> attackable.onAttack(damage));

        gameObject.destroy();
    }
}
