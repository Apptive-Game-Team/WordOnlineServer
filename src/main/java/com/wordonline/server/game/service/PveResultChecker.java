package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.dto.Master;

public class PveResultChecker extends ResultChecker {
    private boolean cleared = false;
    private boolean failed = false;
    private int objectiveObjectId = -1;
    private boolean objectiveSeen = false;

    public PveResultChecker(SessionObject sessionObject) {
        super(sessionObject);
    }

    public void setObjective(GameObject objective) {
        setObjectiveId(objective == null ? -1 : objective.getId());
    }

    public void setObjectiveId(int objectiveObjectId) {
        this.objectiveObjectId = objectiveObjectId;
        this.objectiveSeen = false;
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
        if (objectiveObjectId >= 0) {
            GameObject objective = getSessionObject().getGameContext().getGameObjects().stream()
                    .filter(o -> o.getId() == objectiveObjectId)
                    .findFirst()
                    .orElse(null);

            if (objective == null) {
                if (objectiveSeen) {
                    setCleared();
                }
            } else {
                objectiveSeen = true;

                Mob objectiveMob = objective.getComponent(Mob.class);
                boolean objectiveDestroyed = objective.isDestroyed();
                boolean objectiveHpDepleted = objectiveMob != null && objectiveMob.getHp() <= 0;

                if (objectiveDestroyed || objectiveHpDepleted) {
                    setCleared();
                }
            }
        }

        return cleared || failed;
    }
}
