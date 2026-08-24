package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class RigidBody extends Component {

    private Vector3 velocity = new Vector3(0, 0, 0);
    private final int mass;

    public float getInvMass() {
        if (mass == -1) {
            return 0;
        }
        return 1f / mass;
    }

    public void addVelocity(Vector3 velocity) {
        log.trace("added velocity: {}", velocity);

        if (velocity.hasNaN()) {
            return;
        }

        this.velocity.add(velocity);
    }

    public void applyVelocity() {
        float deltaTime = gameObject.getGameContext().getDeltaTime();
        Vector3 deltaPosition = velocity.multiply(deltaTime);
        Vector3 nextPosition = gameObject.getPosition().plus(deltaPosition);

        // the arguments are computed whether or not the level is on, so the trace calls are
        // guarded: this runs for every rigid body every frame
        if (log.isTraceEnabled()) {
            log.trace("velocity: {}", velocity);
            log.trace("delta time: {}", deltaTime);
            log.trace("delta position: {}", deltaPosition);
            log.trace("next position: {}", nextPosition);
        }

        gameObject.setPosition(nextPosition);

        if (log.isTraceEnabled()) {
            log.trace("position: {}", gameObject.getPosition());
        }

        velocity.clear();
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {

    }

    @Override
    public void onDestroy() {

    }

    public RigidBody(GameObject gameObject, int mass) {
        super(gameObject);
        this.mass = mass;
    }
}
