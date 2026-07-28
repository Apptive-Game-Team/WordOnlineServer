package com.wordonline.server.game.domain.object.component.mob.directive;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.dto.Status;

import java.util.Optional;

public class RallyMoveDirective extends Component implements MovementDirective {
    private static final int PRIORITY = 100;
    private static final float ARRIVAL_DISTANCE = 0.8f;

    private final GameObject rallyTarget;
    private final float combatRange;

    public RallyMoveDirective(GameObject gameObject, GameObject rallyTarget, float combatRange) {
        super(gameObject);
        this.rallyTarget = rallyTarget;
        this.combatRange = combatRange;
    }

    @Override
    public void start() {
    }

    @Override
    public void update() {
        if (rallyTarget == null || rallyTarget.getStatus() == Status.Destroyed) {
            gameObject.removeComponent(this);
        }
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    public Optional<Vector3> getMoveTarget(GameObject self) {
        if (rallyTarget == null || rallyTarget.getStatus() == Status.Destroyed) {
            return Optional.empty();
        }

        return Optional.of(rallyTarget.getPosition());
    }

    @Override
    public float getArrivalDistance() {
        return ARRIVAL_DISTANCE;
    }

    @Override
    public boolean suppressCombat() {
        return true;
    }

    @Override
    public boolean allowsCombatTarget(GameObject self, GameObject target) {
        return rallyTarget != null
                && rallyTarget.getStatus() != Status.Destroyed
                && rallyTarget.getPosition().distance(target.getPosition()) <= combatRange;
    }
}
