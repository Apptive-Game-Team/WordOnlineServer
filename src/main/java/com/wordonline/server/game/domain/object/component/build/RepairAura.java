package com.wordonline.server.game.domain.object.component.build;

import com.wordonline.server.game.domain.debug.GizmoCategory;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.dto.Master;

/**
 * Freezes the lifetime of nearby allied objects: every tick it calls
 * {@link TimedSelfDestroyer#recover(float)} with this tick's delta time, canceling that tick's
 * decay for anything with one in range. It does not heal, and it never touches its own
 * {@link TimedSelfDestroyer}.
 */
public class RepairAura extends Component {

    private final float radius;

    public RepairAura(GameObject gameObject, float radius) {
        super(gameObject);
        this.radius = radius;
    }

    @Override
    public void start() {
        gameObject.drawCircle(Vector3.ZERO, radius, GizmoCategory.AreaOfEffect);
    }

    @Override
    public void update() {
        if (gameObject.getMaster() == Master.None) {
            return;
        }

        float deltaTime = getGameContext().getDeltaTime();
        getGameContext().overlapSphereAll(gameObject, radius).stream()
                .filter(this::canRepair)
                .forEach(target -> target.getComponent(TimedSelfDestroyer.class).recover(deltaTime));
    }

    @Override
    public void onDestroy() {
    }

    private boolean canRepair(GameObject target) {
        if (target == gameObject) return false;
        if (target.getMaster() != gameObject.getMaster()) return false;
        return target.getComponent(TimedSelfDestroyer.class) != null;
    }
}
