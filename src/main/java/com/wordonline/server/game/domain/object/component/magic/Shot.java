package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.physic.Collidable;
import com.wordonline.server.game.dto.Status;
import lombok.Getter;

import java.util.List;

public class Shot extends MagicComponent implements Collidable {
    public static final int SPEED = 2;

    @Getter
    private Vector2 direction;
    private final int damage;

    public Shot(GameObject gameObject, int damage) {
        super(gameObject);
        this.damage = damage;
    }

    public void setTarget(Vector2 targetPosition) {
        direction = (targetPosition.subtract(gameObject.getPosition().toVector2()));
    }

    @Override
    public void update() {
        if (direction == null) {
            return;
        }
        gameObject.setPosition(gameObject.getPosition().plus(direction.multiply(SPEED * gameObject.getGameContext().getDeltaTime())));
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
        direction = Vector2.ZERO;
        gameObject.setStatus(Status.Attack);
        otherObject.setStatus(Status.Damaged);
        attackables.forEach(attackable -> attackable.onDamaged(new AttackInfo(damage, gameObject.getElement().total())));

        gameObject.destroy();
    }
}
