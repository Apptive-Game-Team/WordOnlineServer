package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.component.Component;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class RigidBody extends Component {

    private Vector2 velocity = new Vector2(0, 0);
    private final int mess;

    public float getInvMess() {
        if (mess == -1) {
            return 0;
        }
        return 1f / mess;
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

    @Override
    public void start() {

    }

    @Override
    public void update() {

    }

    @Override
    public void onDestroy() {

    }

    public RigidBody(GameObject gameObject, int mess) {
        super(gameObject);
        this.mess = mess;
    }
}
