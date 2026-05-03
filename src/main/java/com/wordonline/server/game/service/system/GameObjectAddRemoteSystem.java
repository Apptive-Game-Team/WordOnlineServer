package com.wordonline.server.game.service.system;

import java.util.ArrayList;
import java.util.List;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.service.GameContext;

@org.springframework.stereotype.Component
public class GameObjectAddRemoteSystem implements GameSystem {

    @Override
    public void update(GameContext gameContext) {
        List<GameObject> toRemove = gameContext.getGameObjects()
                .stream()
                .filter(GameObject::isDestroyed)
                .toList();

        // Apply Destroyed GameObject
        gameContext.getGameObjects().removeAll(toRemove);

        // Apply Added and Removed Component
        for (GameObject go : gameContext.getGameObjects()) {
            go.flushComponents();
        }

        // Apply Created GameObject
        gameContext.getGameObjects().addAll(gameContext.getGameSessionData().gameObjectsToAdd);
        gameContext.getGameSessionData().gameObjectsToAdd.clear();
    }
}
