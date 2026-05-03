package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.util.CollisionChecker;

import java.util.List;

public class RollingRock extends Shot {

    private static final int MAX_BOUNCES = 2;
    private static final float BOUNCE_SEPARATION = 0.05f;

    private final float radius;

    private Vector3 currentDirection;
    private int bounceCount;
    private Integer collisionLockTargetId;

    public RollingRock(GameObject gameObject, int damage, float speed, float radius) {
        super(gameObject, damage, speed);
        this.radius = radius;
    }

    @Override
    public void setTarget(Vector3 targetPosition) {
        Vector3 flatTarget = new Vector3(targetPosition.getX(), targetPosition.getY(), 0);
        Vector3 flatOrigin = new Vector3(gameObject.getPosition().getX(), gameObject.getPosition().getY(), 0);
        currentDirection = flatTarget.subtract(flatOrigin).normalize();
    }

    @Override
    public Vector3 getDirection() {
        return currentDirection;
    }

    @Override
    public void update() {
        if (currentDirection == null || currentDirection.equals(Vector3.ZERO)) {
            return;
        }

        clearCollisionLockIfSeparated();

        Vector3 nextPosition = gameObject.getPosition()
                .plus(currentDirection.multiply(speed * getGameContext().getDeltaTime()));
        nextPosition = new Vector3(nextPosition.getX(), nextPosition.getY(), 0);

        BorderBounceResult borderBounceResult = resolveBorderBounce(nextPosition);
        if (borderBounceResult.bounced()) {
            if (!consumeBounce()) {
                gameObject.destroy();
                return;
            }

            currentDirection = borderBounceResult.direction().normalize();
            nextPosition = borderBounceResult.position();
        }

        gameObject.setPosition(nextPosition);
    }

    @Override
    public void onCollision(GameObject otherObject) {
        if (otherObject == gameObject || otherObject.getMaster() == gameObject.getMaster()) {
            return;
        }

        if (collisionLockTargetId != null && collisionLockTargetId == otherObject.getId()) {
            return;
        }

        List<Damageable> damageables = otherObject.getComponents(Damageable.class);
        if (damageables.isEmpty()) {
            return;
        }

        AttackInfo attackInfo = new AttackInfo(damage, gameObject.getElement().total());
        otherObject.setStatus(Status.Damaged);
        damageables.forEach(damageable -> damageable.onDamaged(attackInfo));

        collisionLockTargetId = otherObject.getId();

        if (!consumeBounce()) {
            gameObject.destroy();
            return;
        }

        Vector3 normal = resolveCollisionNormal(otherObject);
        Vector3 reflected = reflect(currentDirection, normal).normalize();
        if (reflected.equals(Vector3.ZERO)) {
            reflected = currentDirection.multiply(-1f);
        }

        currentDirection = new Vector3(reflected.getX(), reflected.getY(), 0).normalize();
        gameObject.setStatus(Status.Attack);
        gameObject.setPosition(gameObject.getPosition().plus(currentDirection.multiply(BOUNCE_SEPARATION)));
    }

    private void clearCollisionLockIfSeparated() {
        if (collisionLockTargetId == null) {
            return;
        }

        GameObject lockedTarget = getGameContext().getGameObjects()
                .stream()
                .filter(gameObject1 -> gameObject1.getId() == collisionLockTargetId)
                .findFirst()
                .orElse(null);

        if (lockedTarget == null || lockedTarget.isDestroyed() || !CollisionChecker.isColliding(gameObject, lockedTarget)) {
            collisionLockTargetId = null;
        }
    }

    private BorderBounceResult resolveBorderBounce(Vector3 nextPosition) {
        float minX = GameConfig.X_MID - GameConfig.X_BOUND + radius;
        float maxX = GameConfig.X_MID + GameConfig.X_BOUND - radius;
        float minY = GameConfig.Y_MID - GameConfig.Y_BOUND + radius;
        float maxY = GameConfig.Y_MID + GameConfig.Y_BOUND - radius;

        float nextX = nextPosition.getX();
        float nextY = nextPosition.getY();
        float dirX = currentDirection.getX();
        float dirY = currentDirection.getY();
        boolean bounced = false;

        if (nextX <= minX || nextX >= maxX) {
            dirX *= -1f;
            nextX = Math.clamp(nextX, minX, maxX);
            nextX += dirX > 0 ? BOUNCE_SEPARATION : -BOUNCE_SEPARATION;
            bounced = true;
        }

        if (nextY <= minY || nextY >= maxY) {
            dirY *= -1f;
            nextY = Math.clamp(nextY, minY, maxY);
            nextY += dirY > 0 ? BOUNCE_SEPARATION : -BOUNCE_SEPARATION;
            bounced = true;
        }

        float clampedX = Math.clamp(nextX, minX, maxX);
        float clampedY = Math.clamp(nextY, minY, maxY);

        return new BorderBounceResult(
                bounced,
                new Vector3(dirX, dirY, 0),
                new Vector3(clampedX, clampedY, 0)
        );
    }

    private Vector3 resolveCollisionNormal(GameObject otherObject) {
        Vector3 normal = gameObject.getPosition()
                .subtract(otherObject.getPosition());

        if (normal.getX() == 0f && normal.getY() == 0f) {
            CircleCollider selfCollider = gameObject.getFirstCircleCollider()
                    .orElse(null);
            CircleCollider otherCollider = otherObject.getFirstCircleCollider()
                    .orElse(null);

            if (selfCollider != null && otherCollider != null) {
                normal = selfCollider.getDisplacement(otherCollider);
            }
        }

        if (normal == null || (normal.getX() == 0f && normal.getY() == 0f)) {
            return currentDirection.multiply(-1f).normalize();
        }

        return new Vector3(normal.getX(), normal.getY(), 0).normalize();
    }

    private Vector3 reflect(Vector3 incomingDirection, Vector3 normal) {
        return incomingDirection.subtract(normal.multiply(2f * incomingDirection.dot(normal)));
    }

    private boolean consumeBounce() {
        if (bounceCount >= MAX_BOUNCES) {
            return false;
        }

        bounceCount++;
        return true;
    }

    private record BorderBounceResult(boolean bounced, Vector3 direction, Vector3 position) {
    }
}
