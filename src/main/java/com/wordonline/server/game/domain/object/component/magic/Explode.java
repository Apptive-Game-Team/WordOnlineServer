package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Attackable;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameLoop;

import java.util.List;

public class Explode extends MagicComponent {
    public static final int EXPLODE_DELAY = 2;
    public static final int EXPLODE_RADIUS = 10;

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
                List<Attackable> attackables = otherObject.getComponents()
                        .stream()
                        .filter(component -> component instanceof Attackable)
                        .map(component -> (Attackable) component)
                        .toList();
                if (attackables.isEmpty()) {
                    continue;
                }
                otherObject.setStatus(Status.Damaged);
                attackables.forEach(attackable -> attackable.onAttack(damage));
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
