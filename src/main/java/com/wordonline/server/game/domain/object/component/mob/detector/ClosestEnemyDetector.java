package com.wordonline.server.game.domain.object.component.mob.detector;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.service.GameLoop;

public class ClosestEnemyDetector implements Detector {
    private final GameLoop gameLoop;

    public ClosestEnemyDetector(GameLoop gameLoop) {
        this.gameLoop = gameLoop;
    }

    @Override
    public GameObject detect(GameObject self) {
        GameObject closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (GameObject target : gameLoop.getGameSessionData().gameObjects) {
            if (target.getMaster() == self.getMaster() || target.getComponents().stream().noneMatch(component -> component instanceof Damageable)) continue;

            double distance = self.getPosition().distance(target.getPosition());
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = target;
            }
        }

        return closest;
    }
}
