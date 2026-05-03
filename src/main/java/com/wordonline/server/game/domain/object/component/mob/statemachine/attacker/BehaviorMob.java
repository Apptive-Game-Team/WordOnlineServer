package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.Stat;
import com.wordonline.server.game.domain.debug.GizmoCategory;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.mob.detector.ClosestEnemyDetector;
import com.wordonline.server.game.domain.object.component.mob.detector.Detector;
import com.wordonline.server.game.domain.object.component.mob.pathfinder.PathFinder;
import com.wordonline.server.game.domain.object.component.mob.pathfinder.SimplePathFinder;
import com.wordonline.server.game.domain.object.component.mob.statemachine.StateMachineMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Predicate;

@Slf4j
public class BehaviorMob extends StateMachineMob {

    PathFinder pathFinder;
    Detector detector;
    GameObject target = null;
    float targetRadius;
    RigidBody rigidBody;
    @Getter Stat attackInterval;
    protected float attackRange;
    private Master observedMaster;
    @Setter
    Predicate<GameObject> behavior = null;

    @Override
    public void onDeath() {
        gameObject.destroy();
    }

    @Override
    public void start() {
        observedMaster = gameObject.getMaster();
        setState(new IdleState());
        rigidBody = gameObject.getComponent(RigidBody.class);
        if (attackRange > 0f) {
            gameObject.drawCircle(Vector3.ZERO, attackRange, GizmoCategory.AttackRange);
        }
    }

    public BehaviorMob(GameObject gameObject, int maxHp, float speed, int targetMask, float attackInterval, float attackRange, Predicate<GameObject> behavior) {
        super(gameObject, maxHp, speed);
        this.pathFinder = new SimplePathFinder();
        //this.pathFinder = new AstarPathFinder(GameConfig.WIDTH,GameConfig.HEIGHT,1f);
        this.detector = new ClosestEnemyDetector(getGameContext(), targetMask);
        this.attackInterval = new Stat(attackInterval);
        this.attackRange = attackRange;
        this.behavior = behavior;
    }

    public void setStun(float duration)
    {
        setState(new StunState(duration));
    }
    public void setIdle()
    {
        setState(new IdleState());
    }

    protected void resetTarget() {
        target = null;
        targetRadius = 0f;
    }

    protected boolean isValidTarget(GameObject target) {
        return target != null
                && target.getStatus() != Status.Destroyed
                && target.getMaster() != gameObject.getMaster();
    }

    @Override
    public void update() {
        if (observedMaster == null) {
            observedMaster = gameObject.getMaster();
        } else if (observedMaster != gameObject.getMaster()) {
            observedMaster = gameObject.getMaster();
            resetTarget();
            setState(new IdleState());
        }

        super.update();
    }

    public class StunState extends State {
        private final float duration;
        float timer;
        public StunState(float duration) {
            this.duration = duration;
        }
        @Override
        public void onEnter() {

        }

        @Override
        public void onExit() {

        }

        @Override
        public void onUpdate() {
            timer += getGameContext().getDeltaTime();
            if (timer > duration) {
                setState(new IdleState());
            }

        }
    }

    public class IdleState extends State {
        float timer;
        @Override
        public void onEnter() {
            timer = Detector.DETECTING_INTERVAL + 1;
        }

        @Override
        public void onExit() {
        }

        @Override
        public void onUpdate() {
            timer += getGameContext().getDeltaTime();
            if (timer > Detector.DETECTING_INTERVAL) {
                target = detector.detect(gameObject);
                if (target != null) {
                    setState(new MoveState());
                    targetRadius = target.getFirstCircleCollider().get().getRadius();
                    return;
                }
                timer = 0;
            }
        }
    }

    public class MoveState extends State {
        float timer;
        List<Vector2> path;
        @Override
        public void onEnter() {
            if (!isValidTarget(target)) {
                resetTarget();
                setState(new IdleState());
                return;
            }
            path = pathFinder.findPath(gameObject.getPosition().toVector2(), target.getPosition().toVector2());
        }

        @Override
        public void onExit() {

        }

        @Override
        public void onUpdate() {
            if (!isValidTarget(target) || path == null || path.isEmpty()) {
                resetTarget();
                setState(new IdleState());
                return;
            }

            log.trace("State : {}", currentState);
            Vector2 currentPosition = gameObject.getPosition().toVector2();
            log.trace("Path Remain Distance : {}",currentPosition.distance(path.get(0)));
            log.trace("Target Distance : {}",gameObject.getPosition().distance(target.getPosition()) - targetRadius);
            // Check if we reached the next path point
            if (currentPosition.distance(path.get(0)) < PathFinder.REACH_THRESHOLD) {
                path.remove(0);
                if (path.isEmpty()) {
                    setState(new IdleState());
                    return;
                }
            }

            if (gameObject.getPosition().distance(target.getPosition()) - targetRadius <= attackRange - 0.1f) {
                setState(new AttackState());
                return;
            }
          
            timer += getGameContext().getDeltaTime();
            if (timer > Detector.DETECTING_INTERVAL) {
                GameObject newTarget = detector.detect(gameObject);
                if (newTarget != null && newTarget != target) {
                    target = newTarget;
                    targetRadius = newTarget.getFirstCircleCollider().get().getRadius();
                    path = pathFinder.findPath(gameObject.getPosition().toVector2(), target.getPosition().toVector2());
                    if (path.isEmpty()) return;
                }
                timer = 0f;
            }

            Vector2 nextPoint = path.get(0);
            Vector2 direction = nextPoint.subtract(currentPosition).normalize();

            Vector2 velocity = direction.multiply(speed.total());

            if (rigidBody == null) {
                log.warn("[MobMoveSkipped] {} has no RigidBody; skipping move update", gameObject.getType());
                setState(new IdleState());
                return;
            }

            rigidBody.addVelocity(velocity.toVector3());
        }
    }

    public class AttackState extends State {
        private float timer = 0;
        @Override
        public void onEnter() {

        }

        @Override
        public void onExit() {

        }

        @Override
        public void onUpdate() {
            if (!isValidTarget(target)) {
                resetTarget();
                setState(new IdleState());
                return;
            }
            timer += getGameContext().getDeltaTime();
            if (gameObject.getPosition().distance(target.getPosition()) - targetRadius > attackRange) {
                setState(new MoveState());
            } else if (timer > attackInterval.total()) {
                timer = 0;

                if (!behavior.test(target)) {
                    setState(new IdleState());
                    return;
                }
            }
        }
    }
}


