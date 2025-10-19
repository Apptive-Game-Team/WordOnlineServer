package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class RigidBody extends Component {

    private Vector2 velocity = new Vector2(0, 0);
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
        log.trace("delta time: {}", gameObject.getGameContext().getDeltaTime());
        log.trace("delta position: {}", velocity.multiply(gameObject.getGameContext().getDeltaTime()));
        log.trace("next position: {}", gameObject.getPosition().plus(velocity.multiply(gameObject.getGameContext().getDeltaTime())));
        gameObject.setPosition(
                gameObject.getPosition().plus(velocity.multiply(gameObject.getGameContext().getDeltaTime()))
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

    public RigidBody(GameObject gameObject, int mass) {
        super(gameObject);
        this.mass = mass;
    }

}