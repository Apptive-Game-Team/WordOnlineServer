package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;

public class TimedMassPush extends Component {

    private final GameObject source;
    private Vector3 direction;
    private float speed;
    private float remaining;

    private TimedMassPush(
            GameObject target,
            GameObject source,
            Vector3 direction,
            float speed,
            float duration) {
        super(target);
        this.source = source;
        refresh(direction, speed, duration);
    }

    public static void apply(
            GameObject target,
            GameObject source,
            Vector3 direction,
            float speed,
            float duration) {
        if (target == null || source == null || direction == null || speed <= 0f || duration <= 0f) {
            return;
        }

        RigidBody rigidBody = target.getComponent(RigidBody.class);
        if (rigidBody == null || ForcedMovement.massMultiplier(rigidBody.getMass()) == 0f) {
            return;
        }

        TimedMassPush existing = target.getComponents(TimedMassPush.class).stream()
                .filter(push -> push.source == source)
                .findFirst()
                .orElseGet(() -> target.getComponentsToAdd().stream()
                        .filter(TimedMassPush.class::isInstance)
                        .map(TimedMassPush.class::cast)
                        .filter(push -> push.source == source)
                        .findFirst()
                        .orElse(null));

        if (existing != null) {
            existing.refresh(direction, speed, duration);
            return;
        }

        target.addComponent(new TimedMassPush(target, source, direction, speed, duration));
    }

    private void refresh(Vector3 direction, float speed, float duration) {
        this.direction = direction.normalize();
        this.speed = speed;
        this.remaining = duration;
    }

    @Override
    public void start() {
    }

    @Override
    public void update() {
        float dt = getGameContext().getDeltaTime();
        if (dt <= 0f || remaining <= 0f) {
            expire();
            return;
        }

        RigidBody rigidBody = gameObject.getComponent(RigidBody.class);
        if (rigidBody == null) {
            expire();
            return;
        }

        float activeFraction = Math.min(remaining / dt, 1f);
        float velocity = speed * ForcedMovement.massMultiplier(rigidBody.getMass()) * activeFraction;
        rigidBody.addVelocity(direction.multiply(velocity));

        remaining -= dt;
        if (remaining <= 0f) {
            expire();
        }
    }

    private void expire() {
        gameObject.removeComponent(this);
    }

    @Override
    public void onDestroy() {
    }
}
