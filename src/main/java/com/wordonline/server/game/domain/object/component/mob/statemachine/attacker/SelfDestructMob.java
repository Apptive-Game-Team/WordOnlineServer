package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.debug.GizmoCategory;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.physic.Collidable;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

public class SelfDestructMob extends BehaviorMob implements Collidable {

    private static final float ATTACK_THRESHOLD = 0.3f;
    private final int damage;
    private final float explosionRange;
    private float selfRadius;

    public SelfDestructMob(GameObject gameObject, int maxHp,
                           float speed, int targetMask, int damage, float attackInterval, float attackRange) {
        // Self-destruct mobs should commit as soon as they can collide, so they do not use the
        // shared attack interval or explosion radius as their attack-state trigger distance.
        super(gameObject, maxHp, speed, targetMask, 0f, 0f, null);
        setBehavior(predicate);
        this.damage = damage;
        this.explosionRange = attackRange;
    }

    private final Predicate<GameObject> predicate = (target) -> {
        Mob mob = target.getComponent(Mob.class);

        if (mob == null) return false;

        // The target filtering based on ground/air is now handled by the ClosestEnemyDetector using targetMask
        // This predicate now only checks if the target is a Mob and transitions to AttackingState.
        setState(new AttackingState(mob));
        return true;
    };

    @Override
    public void start() {
        super.start();
        selfRadius = gameObject.getFirstCircleCollider()
                .orElseThrow()
                .getRadius();
        attackRange = selfRadius + ATTACK_THRESHOLD;
        gameObject.drawCircle(Vector3.ZERO, explosionRange, GizmoCategory.AreaOfEffect);
    }

    @Override
    public void onDeath() {
        explode();
        super.onDeath();
    }

    @Override
    public void onCollision(GameObject otherObject) {
        if (otherObject.getComponent(Mob.class) != null && otherObject.getMaster() != gameObject.getMaster()) { // Only explode if colliding with a valid target
            explode();
            gameObject.destroy();
        }
    }



    private void explode() {
        AttackInfo attackInfo = new AttackInfo(damage, gameObject.getElement().total());
        getGameContext().getPhysics()
                .overlapSphereAll(gameObject, explosionRange)
                .forEach(target -> {
                    target.getComponentOptional(Mob.class)
                            .filter(mob -> mob.gameObject.getMaster() == gameObject.getMaster())
                            .ifPresent(mob -> mob.onDamaged(attackInfo));
                });
    }

    @RequiredArgsConstructor
    public class AttackingState extends State {

        private final Mob target;
        private float targetRadius;

        private Vector3 startPos;

        @Override
        public void onEnter() {
            gameObject.getComponentOptional(ZPhysics.class)
                    .ifPresent(zPhysics -> zPhysics.lockHover(this));
            startPos = new Vector3(gameObject.getPosition());
            targetRadius = target.gameObject.getFirstCircleCollider()
                    .orElseThrow()
                    .getRadius();
        }

        @Override
        public void onExit() {
            gameObject.getComponentOptional(ZPhysics.class)
                    .ifPresent(zPhysics -> zPhysics.unlockHover(this));
        }

        @Override
        public void onUpdate() {
            if (target.gameObject.isDestroyed()) {
                setState(new IdleState());
                return;
            }

            // Move towards target
            float startZ = startPos.getZ();
            float lastZ = gameObject.getPosition().getZ();
            float targetZ = target.gameObject.getPosition().getZ();

            float t = (lastZ - startZ) / (targetZ - startZ);

            Vector3 nextPos = Vector3.lerp(startPos, target.gameObject.getPosition(), t);
            nextPos.setZ(lastZ);
            gameObject.setPosition(nextPos);

            // Check if mob is close enough to the target to explode (fallback in case collision event doesn't fire immediately)
            if (gameObject.getPosition().distance(target.gameObject.getPosition()) - targetRadius - selfRadius <= ATTACK_THRESHOLD) {
                 explode();
                 gameObject.destroy();
            }
        }
    }
}
