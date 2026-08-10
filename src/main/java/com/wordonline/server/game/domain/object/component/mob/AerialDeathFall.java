package com.wordonline.server.game.domain.object.component.mob;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.dto.Status;

// Delays the death of an aerial mob until it falls back to the ground.
// While falling the mob is Dying: it stops acting, cannot be targeted and cannot be damaged.
public class AerialDeathFall extends Component {

    private static final float GROUND_THRESHOLD = 0.01f;

    private final Mob mob;
    private final ZPhysics zPhysics;
    private boolean landed;

    // Returns true when the death is deferred until the mob lands, false for ground mobs.
    static boolean tryStart(Mob mob) {
        if (!mob.fallsOnDeath()) {
            return false;
        }

        GameObject gameObject = mob.gameObject;
        ZPhysics zPhysics = gameObject.getComponent(ZPhysics.class);
        if (zPhysics == null || !zPhysics.isAerial()) {
            return false;
        }
        if (gameObject.getPosition().getY() - zPhysics.getGroundY() <= GROUND_THRESHOLD) {
            return false;
        }

        gameObject.setStatus(Status.Dying);
        gameObject.addComponent(new AerialDeathFall(mob, zPhysics));
        return true;
    }

    private AerialDeathFall(Mob mob, ZPhysics zPhysics) {
        super(mob.gameObject);
        this.mob = mob;
        this.zPhysics = zPhysics;
    }

    @Override
    public boolean isActiveWhileDying() {
        return true;
    }

    @Override
    public void start() {
        // stop hovering so the physics system applies gravity instead
        zPhysics.lockHover(this);
    }

    @Override
    public void update() {
        if (landed) {
            return;
        }
        if (gameObject.getPosition().getY() - zPhysics.getGroundY() > GROUND_THRESHOLD) {
            return;
        }

        landed = true;
        mob.completeDeath();
    }

    @Override
    public void onDestroy() {
        // the hover lock is intentionally kept: the object is removed right after landing
    }
}
