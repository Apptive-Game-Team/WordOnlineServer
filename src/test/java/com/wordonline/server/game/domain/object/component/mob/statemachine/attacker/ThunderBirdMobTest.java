package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.GameSessionData;
import com.wordonline.server.game.domain.PlayerData;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ThunderBirdMobTest {

    private GameContext gameContext;
    private GameSessionData sessionData;

    @BeforeEach
    void setUp() {
        gameContext = mock(GameContext.class);
        sessionData = new GameSessionData(mock(PlayerData.class), mock(PlayerData.class));
        when(gameContext.getGameSessionData()).thenReturn(sessionData);
        when(gameContext.getDeltaTime()).thenReturn(0.05f);
    }

    @Test
    void stopsDivingAtAnAlreadyDestroyedTarget() {
        GameObject targetObject = groundObject();
        RecordingMob targetMob = new RecordingMob(targetObject);
        targetObject.getComponents().add(targetMob);

        GameObject bird = aerialObject();
        ThunderBirdMob mob = new ThunderBirdMob(bird, 10, 1.5f, TargetMask.GROUND.bit, 5, 3f, 4f);
        bird.getComponents().add(mob);
        mob.start();
        mob.setState(mob.new AttackingState(targetMob));
        Vector3 positionBeforeUpdate = new Vector3(bird.getPosition());
        targetObject.destroy();

        mob.update();

        assertThat(targetMob.takenDamage).isZero();
        assertThat(bird.getPosition()).isEqualTo(positionBeforeUpdate);
    }

    private GameObject aerialObject() {
        GameObject bird = new GameObject(
                Master.LeftPlayer,
                PrefabType.ThunderBird,
                new Vector3(0f, GameConfig.AERIAL_MOB_INIT_HEIGHT, 0f),
                gameContext);
        bird.setStatus(Status.Idle);
        bird.addCollider(new CircleCollider(bird, 0.3f, false));
        bird.getComponents().add(new RigidBody(bird, 1));
        sessionData.gameObjects.add(bird);
        return bird;
    }

    private GameObject groundObject() {
        GameObject target = new GameObject(Master.RightPlayer, PrefabType.RockSlime, new Vector3(1f, 0f, 0f), gameContext);
        target.setStatus(Status.Idle);
        target.addCollider(new CircleCollider(target, 0.5f, false));
        sessionData.gameObjects.add(target);
        return target;
    }

    private static class RecordingMob extends Mob {
        private int takenDamage;

        private RecordingMob(GameObject gameObject) {
            super(gameObject, 100, 0f);
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
