package com.wordonline.server.game.domain.object.component.build;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.Stat;
import com.wordonline.server.game.domain.debug.GizmoCategory;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.util.Beam;
import lombok.Getter;

/**
 * Breathes fire straight forward on a timer and never looks for a target.
 *
 * <p>The flame leaves every {@code attackInterval} seconds whether or not anything stands in
 * front of it, runs from the muzzle to the field edge, and damages every enemy along the way
 * instead of stopping at the first one. Forward is +X for the left player and -X for the right
 * player, the same rule {@link WindPushComponent} uses.
 */
public class FlameBreath extends Component {

    private static final float BREATH_DURATION = 0.35f;
    private static final String PROJECTILE_NAME = "FireShot";

    /**
     * How high above the ground the flame is drawn. Copied from the sea serpent's beam, which is
     * the only other straight attack the client draws; the dragon tower's own art has no mouth
     * height to read yet.
     */
    private static final float MUZZLE_HEIGHT = 1.6f;

    /**
     * Height of the debug box only. The damage check ignores Y, so the flame catches air units at
     * any altitude; this is just tall enough that the drawn lane does not read as ground-only.
     */
    private static final float GIZMO_HEIGHT = GameConfig.AERIAL_MOB_INIT_HEIGHT;

    private final int damage;
    @Getter
    private final Stat attackInterval;
    private final float beamWidth;
    private float timer;

    /**
     * @param beamWidth how far to either side of the flame's axis a body is still caught, the same
     *                  measure the sea serpent's {@code beam_width} carries
     */
    public FlameBreath(GameObject gameObject, int damage, float attackInterval, float beamWidth) {
        super(gameObject);
        this.damage = damage;
        this.attackInterval = new Stat(attackInterval);
        this.beamWidth = beamWidth;
    }

    @Override
    public void start() {
        Vector3 origin = gameObject.getPosition().grounded();
        Vector3 end = fieldEdgeAhead(origin);
        float length = Math.abs(end.getX() - origin.getX());
        gameObject.drawBox(
                new Vector3((end.getX() - origin.getX()) / 2f, 0f, 0f),
                new Vector3(length, GIZMO_HEIGHT, beamWidth * 2f),
                GizmoCategory.AreaOfEffect);
    }

    @Override
    public void update() {
        timer += getGameContext().getDeltaTime();
        if (timer < attackInterval.total()) {
            return;
        }

        timer = 0f;
        breathe();
    }

    @Override
    public void onDestroy() {
    }

    private void breathe() {
        // an unowned tower has no side to face, so it has nothing to fire at
        if (gameObject.getMaster() == Master.None) {
            return;
        }

        Vector3 origin = gameObject.getPosition().grounded();
        Vector3 end = fieldEdgeAhead(origin);
        AttackInfo attackInfo = new AttackInfo(damage, gameObject.getElement().total())
                .withAttacker(gameObject);

        for (GameObject target : Beam.targetsAlong(gameObject, origin, end, beamWidth)) {
            target.getComponent(Damageable.class).onDamaged(attackInfo, BREATH_DURATION);
        }

        getGameContext().getObjectsInfoDtoBuilder().createProjection(
                gameObject.getPosition().withY(MUZZLE_HEIGHT),
                end.withY(MUZZLE_HEIGHT),
                PROJECTILE_NAME,
                BREATH_DURATION);
        gameObject.setStatus(Status.Attack);
    }

    /**
     * Where the flame stops: the edge of the field in front of the tower, on the tower's own row.
     * The reach is the map, not a range, so nothing here reads a parameter.
     */
    private Vector3 fieldEdgeAhead(Vector3 origin) {
        float edgeX = gameObject.getMaster() == Master.RightPlayer
                ? GameConfig.X_MID - GameConfig.X_BOUND
                : GameConfig.X_MID + GameConfig.X_BOUND;
        return new Vector3(edgeX, 0f, origin.getZ());
    }
}
