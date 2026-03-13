package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.dto.Master;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PveResultChecker extends ResultChecker {
    private boolean cleared = false;
    private boolean failed = false;

    private List<Integer> objectiveIds = List.of();
    private final Set<Integer> seenObjectives = new HashSet<>();

    public PveResultChecker(SessionObject sessionObject) {
        super(sessionObject);
    }

    public void setObjectiveIds(List<Integer> objectiveIds) {
        this.objectiveIds = objectiveIds == null ? List.of() : new ArrayList<>(objectiveIds);
        this.seenObjectives.clear();
    }

    public void setCleared() {
        cleared = true;
        setLoser(Master.RightPlayer);
    }

    public void setFailed() {
        failed = true;
        setLoser(Master.LeftPlayer);
    }

    @Override
    public boolean checkResult() {
        if (!cleared && !failed) {
            // Lose condition: left character dies.
            if (getLoser() == Master.LeftPlayer) {
                setFailed();
            }
        }

        if (!cleared && !failed) {
            checkWinObjectives();
        }

        return cleared || failed;
    }

    private void checkWinObjectives() {
        if (objectiveIds.isEmpty()) {
            return;
        }

        boolean hasValidObjective = false;
        boolean allTerminal = true;

        for (Integer objectiveId : objectiveIds) {
            if (objectiveId == null || objectiveId < 0) {
                continue;
            }
            hasValidObjective = true;

            GameObject objective = getSessionObject().getGameContext().getGameObjects().stream()
                    .filter(o -> o.getId() == objectiveId)
                    .findFirst()
                    .orElse(null);

            if (objective == null) {
                if (!seenObjectives.contains(objectiveId)) {
                    allTerminal = false;
                }
                continue;
            }

            seenObjectives.add(objectiveId);

            if (!isTerminal(objective)) {
                allTerminal = false;
            }
        }

        if (hasValidObjective && allTerminal) {
            setCleared();
        }
    }

    private boolean isTerminal(GameObject objective) {
        Mob objectiveMob = objective.getComponent(Mob.class);
        boolean objectiveDestroyed = objective.isDestroyed();
        boolean objectiveHpDepleted = objectiveMob != null && objectiveMob.getHp() <= 0;
        return objectiveDestroyed || objectiveHpDepleted;
    }
}
