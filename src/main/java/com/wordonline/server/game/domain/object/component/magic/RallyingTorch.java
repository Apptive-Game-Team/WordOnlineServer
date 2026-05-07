package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.mob.directive.RallyMoveDirective;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.BehaviorMob;
import com.wordonline.server.game.dto.Master;

public class RallyingTorch extends MagicComponent {
    private final float duration;
    private boolean landed;
    private float elapsedTime;

    public RallyingTorch(GameObject gameObject, float duration) {
        super(gameObject);
        this.duration = duration;
    }

    @Override
    public void start() {
    }

    @Override
    public void update() {
        if (!landed) {
            fall();
            return;
        }

        elapsedTime += getGameContext().getDeltaTime();
        if (elapsedTime >= duration) {
            gameObject.destroy();
            return;
        }

        getGameContext().getGameSessionData().gameObjects.stream()
                .filter(this::canRally)
                .forEach(this::applyRally);
    }

    @Override
    public void onDestroy() {
    }

    private void fall() {
        Vector3 nextPosition = gameObject.getPosition().plus(0, 0, -Drop.SPEED * getGameContext().getDeltaTime());
        if (nextPosition.getZ() > 0f) {
            gameObject.setPosition(nextPosition);
            return;
        }

        gameObject.setPosition(new Vector3(nextPosition.getX(), nextPosition.getY(), 0f));
        landed = true;
        elapsedTime = 0f;
    }

    private boolean canRally(GameObject target) {
        if (target == gameObject) return false;
        if (gameObject.getMaster() == Master.None) return false;
        if (target.getMaster() != gameObject.getMaster()) return false;
        return target.hasComponent(BehaviorMob.class);
    }

    private void applyRally(GameObject target) {
        if (target.getComponent(RallyMoveDirective.class) != null) {
            return;
        }

        boolean pending = target.getComponentsToAdd().stream()
                .anyMatch(RallyMoveDirective.class::isInstance);
        if (!pending) {
            target.addComponent(new RallyMoveDirective(target, gameObject));
        }
    }
}
