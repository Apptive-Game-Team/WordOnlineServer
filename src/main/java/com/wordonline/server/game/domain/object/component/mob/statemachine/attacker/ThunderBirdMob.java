package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import java.util.function.Predicate;

import javax.swing.text.Position;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;

import lombok.RequiredArgsConstructor;

public class ThunderBirdMob extends BehaviorMob {

    private final int ATTACKABLE_HEIGHT = 3;
    private final int ATTACK_DURATION = 1;
    private final int damage;

    public ThunderBirdMob(GameObject gameObject, int maxHp,
            float speed, int targetMask, int damage, float attackInterval, float attackRange) {
        super(gameObject, maxHp, speed, targetMask, attackInterval, attackRange, null);
        setBehavior(predicate);
        this.damage = damage;
    }

    private final Predicate<GameObject> predicate = (target) -> {
        Mob mob = target.getComponent(Mob.class);

        if (mob == null) return false;

        if (gameObject.getPosition().getZ() < ATTACKABLE_HEIGHT) {
            setState(new FloatingState());
            return true;
        }

        setState(new AttackingState(mob));
        return true;
    };

    @RequiredArgsConstructor
    public class AttackingState extends State {

        private final Mob target;
        private Vector3 startPos;

        @Override
        public void onEnter() {
            gameObject.getComponentOptional(ZPhysics.class)
                    .ifPresent(zPhysics -> zPhysics.lockHover(this));
            startPos = new Vector3(gameObject.getPosition());
        }

        @Override
        public void onExit() {
            gameObject.getComponentOptional(ZPhysics.class)
                    .ifPresent(zPhysics -> zPhysics.unlockHover(this));
        }

        @Override
        public void onUpdate() {
            if (target.gameObject.getPosition().distance(gameObject.getPosition()) > 1) {

                float startZ = startPos.getZ();
                float lastZ = gameObject.getPosition().getZ();
                float targetZ = target.gameObject.getPosition().getZ();

                float t = (lastZ - targetZ) / (startZ - targetZ);

                Vector3 nextPos = Vector3.lerp(startPos, target.gameObject.getPosition(), t);
                nextPos.setZ(lastZ);
                gameObject.setPosition(nextPos);
                return;
            }

            target.onDamaged(new AttackInfo(calculateDamage(startPos), gameObject.getElement().total()));
            setState(new FloatingState());
        }
    }

    private int calculateDamage(Vector3 startPos) {
        return (int) (damage * (startPos.getZ() - gameObject.getPosition().getZ()) / 2);
    }

    public class FloatingState extends State {

        @Override
        public void onEnter() {

        }

        @Override
        public void onExit() {

        }

        @Override
        public void onUpdate() {
            if (gameObject.getPosition().getZ() < ATTACKABLE_HEIGHT) {
                return;
            }

            setState(new IdleState());
        }
    }
}
