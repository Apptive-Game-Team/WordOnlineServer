package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;

public class Drop extends MagicComponent {
    public static final int SPEED = 2;

    private int direction = 0;

    @Override
    public void use(Master master) {
        direction = -1;
    }

    @Override
    public void update() {
        gameObject.setPosition(gameObject.getPosition().add(0, direction * SPEED / GameLoop.FPS));
    }

    public Drop(GameObject gameObject) {
        super(gameObject);
    }
}
