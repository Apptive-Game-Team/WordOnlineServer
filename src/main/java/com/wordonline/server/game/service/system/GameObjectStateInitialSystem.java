package com.wordonline.server.game.service.system;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameContext;

@Component
public class GameObjectStateInitialSystem implements GameSystem {

    @Override
    public void update(GameContext gameContext) {
        gameContext.getGameObjects()
                .stream()
                .filter(gameObject -> gameObject.getStatus() != Status.Idle)
                .forEach(gameObject -> gameObject.setStatus(Status.Idle));
    }
}
