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
            if (!go.getComponentsToAdd().isEmpty()) {
                List<Component> toAdd = new ArrayList<>(go.getComponentsToAdd());
                go.getComponentsToAdd().clear();
                go.getComponents().addAll(toAdd);
                for (Component c : toAdd) {
                    c.start();
                }
            }

            if (!go.getComponentsToRemove().isEmpty()) {
                List<Component> toRem = new ArrayList<>(go.getComponentsToRemove());
                go.getComponentsToRemove().clear();
                for (Component c : toRem) {
                    c.onDestroy();
                }
                go.getComponents().removeAll(toRem);
            }
        }

        // Apply Created GameObject
        gameContext.getGameObjects().addAll(gameContext.getGameSessionData().gameObjectsToAdd);
        gameContext.getGameSessionData().gameObjectsToAdd.clear();
    }
}
