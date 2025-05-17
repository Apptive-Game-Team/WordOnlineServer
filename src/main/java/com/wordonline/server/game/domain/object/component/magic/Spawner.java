package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;

public class Spawner extends MagicComponent {
    public static final int SPAWN_INTERNAL = 2;

    private float counter = 0;
    private boolean isRunning = false;

    @Override
    public void use(Master master) {
        isRunning = true;
    }

    @Override
    public void update() {
        if (!isRunning) {
            return;
        }

        if (counter < SPAWN_INTERNAL) {
            counter += 1.0f / GameLoop.FPS;
        } else {
            counter = 0;
            // TODO - implement spawn logic
        }
    }

    public Spawner(GameObject gameObject) {
        super(gameObject);
    }
}
