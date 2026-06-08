package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.effect.receiver.EffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.directive.RallyMoveDirective;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.BehaviorMob;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Master;

public class RallyingTorch extends MagicComponent {
    private final float duration;
    private final float buffRadius;
    private final float buffDuration;
    private boolean landed;
    private float elapsedTime;

    public RallyingTorch(GameObject gameObject, float duration, float buffRadius, float buffDuration) {
        super(gameObject);
        this.duration = duration;
        this.buffRadius = buffRadius;
        this.buffDuration = buffDuration;
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

        getGameContext().overlapSphereAll(gameObject, buffRadius).stream()
                .filter(this::canInspire)
                .forEach(this::applyInspired);
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

    private boolean canInspire(GameObject target) {
        if (target == gameObject) return false;
        if (gameObject.getMaster() == Master.None) return false;
        if (target.getMaster() != gameObject.getMaster()) return false;
        return target.getComponent(EffectReceiver.class) != null;
    }

    private void applyInspired(GameObject target) {
        target.getComponent(EffectReceiver.class).onReceive(Effect.Inspired, buffDuration);
    }
}
