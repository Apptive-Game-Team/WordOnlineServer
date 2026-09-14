package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Stat;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.IntervalAttacker;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import lombok.Getter;

/**
 * Launches a {@link PrefabType#DragonFlame} straight forward every attack interval and never looks
 * for a target. The flame leaves whether or not anything stands in front of it; where it stops is
 * the projectile's own business. It explodes on the first enemy it touches, and when it touches
 * nothing it flies off the field and the bounds check in GameObject destroys it.
 *
 * <p>Forward comes from the owner's side, the same left and right convention
 * {@link com.wordonline.server.game.domain.object.component.build.WindPushComponent} uses, since a
 * GameObject carries no facing vector.
 */
public class FlameLauncher extends MagicComponent implements IntervalAttacker {

    @Getter
    private final Stat attackInterval;
    private float timer;

    public FlameLauncher(GameObject gameObject, float attackInterval) {
        super(gameObject);
        this.attackInterval = new Stat(attackInterval);
    }

    @Override
    public void update() {
        timer += getGameContext().getDeltaTime();
        if (timer < attackInterval.total()) {
            return;
        }

        timer = 0f;
        launchFlame();
    }

    private void launchFlame() {
        Vector3 forward = forwardDirection();
        if (forward == null) {
            // Master.None has no forward side to aim at, so there is nothing to launch
            return;
        }

        GameObject flame = new GameObject(
                gameObject.getMaster(),
                PrefabType.DragonFlame,
                new Vector3(gameObject.getPosition()),
                getGameContext());
        flame.getComponent(Shot.class).setTarget(fieldEdgeAhead(forward));
        gameObject.setStatus(Status.Attack);
    }

    private Vector3 fieldEdgeAhead(Vector3 forward) {
        float edgeX = forward.getX() > 0
                ? GameConfig.X_MID + GameConfig.X_BOUND
                : GameConfig.X_MID - GameConfig.X_BOUND;
        return new Vector3(edgeX, gameObject.getPosition().getY(), gameObject.getPosition().getZ());
    }

    private Vector3 forwardDirection() {
        Master master = gameObject.getMaster();
        if (master == Master.LeftPlayer) {
            return Vector3.RIGHT;
        }
        if (master == Master.RightPlayer) {
            return Vector3.LEFT;
        }
        return null;
    }
}
