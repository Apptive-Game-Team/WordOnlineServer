package com.wordonline.server.game.domain.object.component.mob.statemachine.slime;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.mob.detector.ClosestEnemyDetector;
import com.wordonline.server.game.domain.object.component.mob.detector.Detector;
import com.wordonline.server.game.domain.object.component.mob.pathfinder.PathFinder;
import com.wordonline.server.game.domain.object.component.mob.pathfinder.SimplePathFinder;
import com.wordonline.server.game.domain.object.component.mob.statemachine.StateMachineMob;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.dto.Status;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Slime extends StateMachineMob {

    PathFinder pathFinder;
    Detector detector;
    GameObject target = null;
    RigidBody rigidBody;
    int damage;


    @Override
    public void onDeath() {
        gameObject.destroy();
    }

    @Override
    public void start() {
        setState(new IdleState());
        rigidBody = gameObject.getComponent(RigidBody.class);
    }

    public Slime(GameObject gameObject, int maxHp, float speed, int damage) {
        super(gameObject, maxHp, speed);
        this.pathFinder = new SimplePathFinder();
        this.detector = new ClosestEnemyDetector(gameObject.getGameLoop());
        this.damage = damage;
    }


    public class IdleState extends State {
        float timer;
        @Override
        public void onEnter() {
            timer = 0;
        }

        @Override
        public void onExit() {
        }

        @Override
        public void onUpdate() {
            timer += gameObject.getGameLoop().deltaTime;
            if (timer > Detector.DETECTING_INTERVAL) {
                target = detector.detect(gameObject);
                if (target != null) {
                    setState(new MoveState());
                    return;
                }
                timer = 0;
            }
        }
    }

    public class MoveState extends State {
        List<Vector2> path;
        @Override
        public void onEnter() {
            if (target == null) {
                setState(new IdleState());
                return;
            }
            path = pathFinder.findPath(gameObject.getPosition(), target.getPosition());
        }

        @Override
        public void onExit() {

        }

        @Override
        public void onUpdate() {
            if (target.getStatus() == Status.Destroyed || path == null || path.isEmpty()) {
                setState(new IdleState());
                return;
            }

            Vector2 currentPosition = gameObject.getPosition();

            // 다음 포인트에 도착했는지 판단
            if (currentPosition.distance(path.get(0)) < PathFinder.REACH_THRESHOLD) {
                path.remove(0);
                if (path.isEmpty()) {
                    setState(new IdleState());
                    return;
                }
            }

            if (gameObject.getPosition().distance(target.getPosition()) < 1) {
                setState(new AttackState());
                return;
            }

            Vector2 nextPoint = path.get(0);
            Vector2 direction = nextPoint.subtract(currentPosition).normalize();

            float speed = getSpeed();
            Vector2 velocity = direction.multiply(speed);

            rigidBody.addVelocity(velocity);
        }
    }

    public class AttackState extends State {
        private final float attackInterval = 1;
        private float timer = 0;
        @Override
        public void onEnter() {

        }

        @Override
        public void onExit() {

        }

        @Override
        public void onUpdate() {
            if (target.getStatus() == Status.Destroyed) {
                setState(new IdleState());
            }
            timer += gameObject.getGameLoop().deltaTime;
            if (gameObject.getPosition().distance(target.getPosition()) > 1) {
                setState(new MoveState());
            } else if (timer > attackInterval) {
                timer = 0;
                Damageable attackable = ((Damageable) target.getComponents().stream()
                        .filter(component -> component instanceof Damageable)
                        .findFirst()
                        .orElse(null));
                if (attackable == null) {
                    setState(new IdleState());
                    return;
                }

                attackable.onDamaged(new AttackInfo(damage, gameObject.getElement()));
                gameObject.setStatus(Status.Attack);
            }
        }
    }
}
