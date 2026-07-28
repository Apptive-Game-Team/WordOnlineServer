package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.util.Physics;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelfDestructMobTest {

    private static final float HOVER_HEIGHT = GameConfig.AERIAL_MOB_INIT_HEIGHT;
    private static final float SELF_RADIUS = 0.3f;
    private static final float ATTACK_THRESHOLD = 0.3f;

    @Test
    void explosionOnlyDamagesActiveEnemies() {
        GameContext gameContext = mock(GameContext.class);
        Physics physics = mock(Physics.class);
        when(gameContext.getPhysics()).thenReturn(physics);

        GameObject windSpirit = objectWithMob(Master.LeftPlayer, gameContext);
        GameObject ally = objectWithMob(Master.LeftPlayer, gameContext);
        GameObject enemy = objectWithMob(Master.RightPlayer, gameContext);
        GameObject destroyedEnemy = objectWithMob(Master.RightPlayer, gameContext);
        destroyedEnemy.setStatus(Status.Destroyed);
        when(physics.overlapSphereAll(any(GameObject.class), anyFloat()))
                .thenReturn(List.of(windSpirit, ally, enemy, destroyedEnemy));

        SelfDestructMob mob = new SelfDestructMob(windSpirit, 10, 1f, TargetMask.ANY.bit, 7, 1f, 2f);
        windSpirit.getComponents().add(mob);
        mob.onDeath();

        assertThat(damageOf(enemy)).isEqualTo(7);
        assertThat(damageOf(ally)).isZero();
        assertThat(damageOf(destroyedEnemy)).isZero();
        assertThat(damageOf(windSpirit)).isZero();
    }

    @Test
    void commitDistanceCoversTheAltitudeItHasToDive() throws Exception {
        GameContext gameContext = mock(GameContext.class);
        GameObject windSpirit = new GameObject(
                Master.LeftPlayer,
                PrefabType.WindSpirit,
                new Vector3(0f, HOVER_HEIGHT, 0f),
                gameContext);
        windSpirit.setStatus(Status.Idle);
        windSpirit.addCollider(new CircleCollider(windSpirit, SELF_RADIUS, false));
        windSpirit.getComponents().add(new RigidBody(windSpirit, 1));
        SelfDestructMob mob = new SelfDestructMob(windSpirit, 10, 1f, TargetMask.ANY.bit, 7, 1f, 2f);
        windSpirit.getComponents().add(mob);

        mob.start();

        // A ground target sits HOVER_HEIGHT below, so a purely horizontal trigger distance can
        // never be reached and the mob would hover instead of diving.
        assertThat(attackRangeOf(mob)).isGreaterThan(HOVER_HEIGHT);
        assertThat(attackRangeOf(mob)).isEqualTo(SELF_RADIUS + ATTACK_THRESHOLD + HOVER_HEIGHT);
    }

    private float attackRangeOf(SelfDestructMob mob) throws Exception {
        Field field = BehaviorMob.class.getDeclaredField("attackRange");
        field.setAccessible(true);
        return (float) field.get(mob);
    }

    private int damageOf(GameObject gameObject) {
        return ((RecordingMob) gameObject.getComponent(Mob.class)).takenDamage;
    }

    private GameObject objectWithMob(Master master, GameContext gameContext) {
        GameObject gameObject = new GameObject(master, PrefabType.WindSpirit, Vector3.ZERO, gameContext);
        gameObject.setStatus(Status.Idle);
        gameObject.getComponents().add(new RecordingMob(gameObject));
        return gameObject;
    }

    private static class RecordingMob extends Mob {
        private int takenDamage;

        private RecordingMob(GameObject gameObject) {
            super(gameObject, 100, 1f);
        }

        @Override
        public void onDamaged(AttackInfo attackInfo) {
            takenDamage += attackInfo.getDamage();
        }

        @Override
        public void onDeath() {
        }

        @Override
        public void start() {
        }

        @Override
        public void onDestroy() {
        }
    }
}
