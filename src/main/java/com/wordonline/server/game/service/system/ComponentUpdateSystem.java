package com.wordonline.server.game.service.system;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameContext;

public class ComponentUpdateSystem implements GameSystem {

    @Override
    public void update(GameContext gameContext) {
        for (GameObject gameObject : gameContext.getGameObjects()) {
            if (gameObject.getStatus() != Status.Destroyed) {
                gameObject.update();
            }
        }
    }
}
