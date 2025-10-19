package com.wordonline.server.game.domain.object.component.mob.detector;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.GameLoop;

public class ClosestEnemyDetector implements Detector {
    private final GameContext gameContext;
    private final int targetMask;

    public ClosestEnemyDetector(GameContext gameContext, int targetMask) {
        this.gameContext = gameContext;
        this.targetMask = targetMask;
    }

    @Override
    public GameObject detect(GameObject self) {
        GameObject closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (GameObject target : gameContext.getGameSessionData().gameObjects) {
            if (target.getMaster() == self.getMaster() || target.getComponents().stream().noneMatch(component -> component instanceof Damageable)) continue;

            if((TargetMask.of(target) & targetMask) == 0) continue;

            double distance = self.getPosition().distance(target.getPosition());
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = target;
            }
        }

        return closest;
    }
}
