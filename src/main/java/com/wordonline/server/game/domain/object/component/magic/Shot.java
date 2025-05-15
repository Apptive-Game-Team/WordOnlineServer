package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;

public class Shot extends MagicComponent {
    public static final int SPEED = 2;

    private int direction = 0;

    @Override
    public void use(Master master) {
        if (master == Master.LeftPlayer) {
            direction = -1;
        } else if (master == Master.RightPlayer) {
            direction = 1;
        }
    }

    @Override
    public void update() {
        gameObject.setPosition(gameObject.getPosition().add(direction * SPEED / GameLoop.FPS, 0));
    }

    public Shot(GameObject gameObject) {
        super(gameObject);
    }
}
