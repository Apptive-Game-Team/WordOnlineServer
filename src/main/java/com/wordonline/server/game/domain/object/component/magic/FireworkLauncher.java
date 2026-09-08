package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;

// Fires PrefabType.FireworkShell at a fixed point in front of the tower every
// attackInterval seconds. Unlike CraterEmber, there is no target search and no
// randomness: the impact point is always gameObject.getPosition() + forward *
// attackOffset, where "forward" comes from the same left/right convention
// WindPushComponent uses (there is no facing vector on GameObject).
public class FireworkLauncher extends MagicComponent {
    private final float attackInterval;
    private final float attackOffset;
    private float elapsed;

    public FireworkLauncher(GameObject gameObject, float attackInterval, float attackOffset) {
        super(gameObject);
        this.attackInterval = Math.max(0.05f, attackInterval);
        this.attackOffset = attackOffset;
    }

    @Override
    public void update() {
        elapsed += getGameContext().getDeltaTime();
        while (elapsed >= attackInterval) {
            elapsed -= attackInterval;
            launchShell();
        }
    }

    private void launchShell() {
        Vector3 direction = forwardDirection();
        if (direction == null) {
            // Master.None has no forward side to aim at: skip this tick's shot
            // rather than guess a direction.
            return;
        }

        Vector3 impactPosition = gameObject.getPosition().plus(direction.multiply(attackOffset));
        new GameObject(gameObject.getMaster(), PrefabType.FireworkShell, impactPosition, getGameContext());
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
