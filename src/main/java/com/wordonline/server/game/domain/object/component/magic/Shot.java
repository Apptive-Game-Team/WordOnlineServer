package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Collidable;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameLoop;

public class Shot extends MagicComponent implements Collidable {
    public static final int SPEED = 2;

    private int direction = 0;

    public Shot(Master master, GameObject gameObject) {
        super(gameObject);
        if (master == Master.LeftPlayer) {
            direction = 1;
        } else if (master == Master.RightPlayer) {
            direction = -1;
        }
    }

    @Override
    public void update() {
        gameObject.setPosition(gameObject.getPosition().add(direction * SPEED * gameObject.getGameLoop().deltaTime, 0));
    }

    @Override
    public void onCollision() {
        direction = 0;
        gameObject.setStatus(Status.Attack);
        gameObject.destroy();
    }
}
