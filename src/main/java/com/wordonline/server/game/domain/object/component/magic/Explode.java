package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.dto.Status;

import java.util.List;

public class Explode extends MagicComponent {
    public static final float EXPLODE_DELAY = 0.5f;
    public static final int EXPLODE_RADIUS = 3;

    private boolean isRunning = false;

    private final int damage;
    private float counter = 0;

    @Override
    public void update() {
        if (!isRunning) {
            return;
        }

        if (counter < EXPLODE_DELAY) {
            counter += gameObject.getGameLoop().deltaTime;
        } else {
            List<GameObject> gameObjects = gameObject.getGameLoop().physics.overlapCircleAll(gameObject, EXPLODE_RADIUS);
            for (GameObject otherObject : gameObjects) {
                List<Damageable> attackables = otherObject.getComponents(Damageable.class);

                if (attackables.isEmpty()) {
                    continue;
                }
                otherObject.setStatus(Status.Damaged);
                attackables.forEach(attackable -> attackable.onDamaged(new AttackInfo(damage, gameObject.getElement())));
            }

            gameObject.setStatus(Status.Attack);
            gameObject.destroy();
        }
    }

    public Explode(GameObject gameObject, int damage) {
        super(gameObject);
        this.damage = damage;
        isRunning = true;
    }
}
