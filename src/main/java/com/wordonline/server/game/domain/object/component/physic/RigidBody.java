package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.component.Component;
import lombok.Getter;

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
        velocity.add(velocity);
    }

    public void applyVelocity() {
        gameObject.setPosition(
                gameObject.getPosition().plus(velocity.multiply(gameObject.getGameLoop().deltaTime))
        );
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
