package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.debug.GizmoCategory;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.dto.Status;
import lombok.Getter;

import java.util.List;

public class Explode extends MagicComponent {
    public static final float EXPLODE_DELAY = 0.5f;
    public static final float EXPLODE_RADIUS = 3f;

    protected final int damage;
    @Getter
    protected final float radius;
    protected final float delay;

    protected boolean isRunning = false;
    protected float counter = 0f;

    protected AttackInfo attackInfo;

    public Explode(GameObject gameObject, int damage) {
        this(gameObject, damage, EXPLODE_RADIUS, EXPLODE_DELAY);
    }

    public Explode(GameObject gameObject, int damage, float radius) {
        this(gameObject, damage, radius, EXPLODE_DELAY);
    }

    public Explode(GameObject gameObject, int damage, float radius, float delay) {
        super(gameObject);
        this.damage = damage;
        this.radius = radius;
        this.delay = delay;
        this.isRunning = true;
        attackInfo = new AttackInfo(damage, gameObject.getElement().total()).withAttacker(gameObject);
    }

    @Override
    public void start() {
        gameObject.drawCircle(Vector3.ZERO, radius, GizmoCategory.AreaOfEffect);
    }

    @Override
    public void update() {
        if (!isRunning) return;

        if (counter < delay) {
            counter += getGameContext().getDeltaTime();
            return;
        }

        List<GameObject> gameObjects = getGameContext().overlapSphereAll(gameObject, radius);

        for (GameObject otherObject : gameObjects) {
            if (otherObject == gameObject) continue;

            handleGameObject(otherObject);
        }
        isRunning = false;
        gameObject.destroy();
    }

    protected void handleGameObject(GameObject targetObject) {
        List<Damageable> attackables = targetObject.getComponents(Damageable.class);

        if (attackables.isEmpty()) return;

        targetObject.setStatus(Status.Damaged);

        attackables.forEach(this::handleDamageable);
    }

    protected void handleDamageable(Damageable damageable) {
        damageable.onDamaged(attackInfo);
    }
}
