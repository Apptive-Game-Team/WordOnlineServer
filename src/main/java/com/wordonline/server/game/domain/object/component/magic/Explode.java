package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameLoop;

public class Explode extends MagicComponent {
    public static final int EXPLODE_DELAY = 2;

    private boolean isRunning = false;

    private float counter = 0;

    @Override
    public void update() {
        if (!isRunning) {
            return;
        }

        if (counter < EXPLODE_DELAY) {
            counter += gameObject.getGameLoop().deltaTime;
        } else {
            // TODO - implement explode logic
             gameObject.setStatus(Status.Attack);
             gameObject.destroy();
        }
    }

    public Explode(GameObject gameObject) {
        super(gameObject);
        isRunning = true;
    }
}
