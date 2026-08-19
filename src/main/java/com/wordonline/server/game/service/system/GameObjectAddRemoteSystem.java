package com.wordonline.server.game.service.system;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.service.GameContext;

@org.springframework.stereotype.Component
public class GameObjectAddRemoteSystem implements GameSystem {

    @Override
    public void update(GameContext gameContext) {
        // Apply Destroyed GameObject
        // removeIf is a single pass over the list; collecting the destroyed objects and calling
        // removeAll made ArrayList search itself once per removed object.
        gameContext.getGameObjects().removeIf(GameObject::isDestroyed);

        // Apply Added and Removed Component
        for (GameObject go : gameContext.getGameObjects()) {
            go.flushComponents();
        }

        // Apply Created GameObject
        gameContext.getGameObjects().addAll(gameContext.getGameSessionData().gameObjectsToAdd);
        gameContext.getGameSessionData().gameObjectsToAdd.clear();
    }
}
