package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class RigidBody extends Component {

    private Vector2 velocity = new Vector2(0, 0);
    private float normalForce = GameConfig.GLOBAL_GRAVITY;
    private float originalZPos;
    private final int mass;

    public float getInvMass() {
        if (mass == -1) {
            return 0;
        }
        return 1f / mass;
    }

    public void addVelocity(Vector2 velocity) {
        log.trace("added velocity: {}", velocity);
        this.velocity.add(velocity);
    }

    public void applyVelocity() {
        log.trace("velocity: {}", velocity);
        log.trace("delta time: {}", gameObject.getGameLoop().deltaTime);
        log.trace("delta position: {}", velocity.multiply(gameObject.getGameLoop().deltaTime));
        log.trace("next position: {}", gameObject.getPosition().plus(velocity.multiply(gameObject.getGameLoop().deltaTime)));
        gameObject.setPosition(
                gameObject.getPosition().plus(velocity.multiply(gameObject.getGameLoop().deltaTime))
        );
        log.trace("position: {}", gameObject.getPosition());

        velocity.clear();
    }

    public void addNormalForce(float force) {
        normalForce += force;
    }

    public void applyZForce() {
        final float dt = gameObject.getGameLoop().deltaTime;
        final Vector3 pos = gameObject.getPosition();
        final float normalVelocity = (normalForce - GameConfig.GLOBAL_GRAVITY) * dt;
        final float newZ = Math.max(originalZPos, pos.getZ() + normalVelocity);
        gameObject.setPosition(new Vector3(pos.getX(), pos.getY(), newZ));
        normalForce = Math.max(0f,normalForce - GameConfig.GLOBAL_GRAVITY);
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
    public RigidBody(GameObject gameObject, int mass, float ZPos) {
        super(gameObject);
        this.mass = mass;
        this.originalZPos = ZPos;
    }

}
