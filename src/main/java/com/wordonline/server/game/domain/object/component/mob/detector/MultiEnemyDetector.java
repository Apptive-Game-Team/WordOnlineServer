package com.wordonline.server.game.domain.object.component.mob.detector;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MultiEnemyDetector implements Detector {

    private final GameContext gameContext;
    private final int targetMask;

    public MultiEnemyDetector(GameContext gameContext, int targetMask) {
        this.gameContext = gameContext;
        this.targetMask = targetMask;
    }

    @Override
    public GameObject detect(GameObject self) {
        List<GameObject> list = detectInRange(self, 1, Double.MAX_VALUE);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 사거리 안의 적을 거리순으로 정렬해서 최대 maxCount개 리턴
     */
    public List<GameObject> detectInRange(GameObject self, int maxCount, double maxRange) {
        List<GameObject> candidates = new ArrayList<>();

        for (GameObject target : gameContext.getGameSessionData().gameObjects) {
            if (target == self) continue;

            // 죽은 애 제외
            if (target.getStatus() == Status.Destroyed) continue;

            // 같은 편 제외
            if (target.getMaster() == self.getMaster()) continue;

            // 맞을 수 없는 애 제외 (Damageable 없는 애)
            boolean hasDamageable = target.getComponents().stream()
                    .anyMatch(c -> c instanceof Damageable);
            if (!hasDamageable) continue;

            // 마스크 체크
            if ((TargetMask.of(target) & targetMask) == 0) continue;

            double distance = self.getPosition().distance(target.getPosition());
            if (distance <= maxRange) {
                candidates.add(target);
            }
        }

        // 거리 가까운 순으로 정렬
        candidates.sort(Comparator.comparingDouble(
                o -> self.getPosition().distance(o.getPosition())
        ));

        // 최대 maxCount개까지만 잘라서 리턴
        if (candidates.size() > maxCount) {
            return new ArrayList<>(candidates.subList(0, maxCount));
        }
        return candidates;
    }
}