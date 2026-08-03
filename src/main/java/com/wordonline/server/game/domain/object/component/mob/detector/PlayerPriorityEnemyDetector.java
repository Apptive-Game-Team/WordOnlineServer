package com.wordonline.server.game.domain.object.component.mob.detector;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.service.GameContext;

import java.util.List;
import java.util.function.Predicate;

public class PlayerPriorityEnemyDetector implements Detector {
    private final PriorityEnemyDetector playerDetector;
    private final ClosestEnemyDetector closestEnemyDetector;

    public PlayerPriorityEnemyDetector(GameContext gameContext, int targetMask) {
        this.playerDetector = new PriorityEnemyDetector(
                gameContext,
                targetMask,
                Double.MAX_VALUE,
                List.of(TargetCategory.PLAYER));
        this.closestEnemyDetector = new ClosestEnemyDetector(gameContext, targetMask);
    }

    @Override
    public GameObject detect(GameObject self) {
        return detect(self, target -> true);
    }

    @Override
    public GameObject detect(GameObject self, Predicate<GameObject> filter) {
        GameObject player = playerDetector.detect(self, filter);
        return player != null ? player : closestEnemyDetector.detect(self, filter);
    }
}
