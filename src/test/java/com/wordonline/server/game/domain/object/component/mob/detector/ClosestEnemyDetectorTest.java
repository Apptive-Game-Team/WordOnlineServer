package com.wordonline.server.game.domain.object.component.mob.detector;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.GameSessionData;
import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.effect.statuseffect.FrenzyStatusEffect;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClosestEnemyDetectorTest {

    private static final float GROUND_HEIGHT = 0f;
    private static final float AIR_HEIGHT = GameConfig.AERIAL_MOB_INIT_HEIGHT;

    private GameContext gameContext;
    private GameSessionData sessionData;

    @BeforeEach
    void setUp() {
        gameContext = mock(GameContext.class);
        sessionData = new GameSessionData(mock(PlayerData.class), mock(PlayerData.class));
        when(gameContext.getGameSessionData()).thenReturn(sessionData);
    }

    @Test
    void filteredDetectionSkipsACloserEnemyOutsideTheAllowedArea() {
        GameObject self = mock(GameObject.class);
        GameObject closerFilteredOut = enemyAt(1f);
        GameObject fartherAllowed = enemyAt(2f);
        GameSessionData sessionData = new GameSessionData(null, null);
        sessionData.gameObjects.addAll(List.of(closerFilteredOut, fartherAllowed));
        GameContext gameContext = mock(GameContext.class);

        when(self.getMaster()).thenReturn(Master.LeftPlayer);
        when(self.getPosition()).thenReturn(Vector3.ZERO);
        when(gameContext.getGameSessionData()).thenReturn(sessionData);

        ClosestEnemyDetector detector = new ClosestEnemyDetector(gameContext, TargetMask.GROUND.bit);

        GameObject detected = detector.detect(self, target -> target == fartherAllowed);

        assertThat(detected).isSameAs(fartherAllowed);
    }

    @Test
    void frenziedMobCanTargetAnotherFrenziedMobWithTheSameNeutralMaster() {
        GameObject self = mock(GameObject.class);
        GameObject otherFrenziedMob = enemyAt(1f);
        FrenzyStatusEffect frenzy = mock(FrenzyStatusEffect.class);
        GameSessionData sessionData = new GameSessionData(null, null);
        sessionData.gameObjects.add(otherFrenziedMob);
        GameContext gameContext = mock(GameContext.class);

        when(self.getMaster()).thenReturn(Master.None);
        when(self.getPosition()).thenReturn(Vector3.ZERO);
        when(self.getComponent(FrenzyStatusEffect.class)).thenReturn(frenzy);
        when(frenzy.isActive()).thenReturn(true);
        when(otherFrenziedMob.getMaster()).thenReturn(Master.None);
        when(gameContext.getGameSessionData()).thenReturn(sessionData);

        ClosestEnemyDetector detector = new ClosestEnemyDetector(gameContext, TargetMask.GROUND.bit);

        assertThat(detector.detect(self)).isSameAs(otherFrenziedMob);
    }

    @Test
    void nonFrenziedMobStillIgnoresTargetsWithTheSameMaster() {
        GameObject self = mock(GameObject.class);
        GameObject ally = enemyAt(1f);
        GameSessionData sessionData = new GameSessionData(null, null);
        sessionData.gameObjects.add(ally);
        GameContext gameContext = mock(GameContext.class);

        when(self.getMaster()).thenReturn(Master.LeftPlayer);
        when(self.getPosition()).thenReturn(Vector3.ZERO);
        when(ally.getMaster()).thenReturn(Master.LeftPlayer);
        when(gameContext.getGameSessionData()).thenReturn(sessionData);

        ClosestEnemyDetector detector = new ClosestEnemyDetector(gameContext, TargetMask.GROUND.bit);

        assertThat(detector.detect(self)).isNull();
    }

    @Test
    void picksClosestAcrossGroundAndAirWhenTargetingAny() {
        GameObject self = enemy(Master.LeftPlayer, 0f, AIR_HEIGHT);
        GameObject farAir = enemy(Master.RightPlayer, 6f, AIR_HEIGHT);
        GameObject closeGround = enemy(Master.RightPlayer, 2f, GROUND_HEIGHT);
        register(farAir, closeGround);

        GameObject detected = new ClosestEnemyDetector(gameContext, TargetMask.ANY.bit).detect(self);

        assertThat(detected).isSameAs(closeGround);
    }

    @Test
    void repeatsTheSameChoiceForTheSameWorldState() {
        GameObject self = enemy(Master.LeftPlayer, 0f, AIR_HEIGHT);
        GameObject air = enemy(Master.RightPlayer, 3f, AIR_HEIGHT);
        GameObject ground = enemy(Master.RightPlayer, 3f, GROUND_HEIGHT);
        register(air, ground);
        ClosestEnemyDetector detector = new ClosestEnemyDetector(gameContext, TargetMask.ANY.bit);

        GameObject first = detector.detect(self);
        GameObject second = detector.detect(self);

        assertThat(first).isSameAs(second);
        assertThat(first).isSameAs(air);
    }

    @Test
    void ignoresAlliesSelfDestroyedAndUninitializedTargets() {
        GameObject self = enemy(Master.LeftPlayer, 0f, AIR_HEIGHT);
        register(self);
        register(enemy(Master.LeftPlayer, 1f, GROUND_HEIGHT));
        GameObject destroyed = enemy(Master.RightPlayer, 2f, GROUND_HEIGHT);
        destroyed.setStatus(Status.Destroyed);
        register(destroyed);
        GameObject uninitialized = new GameObject(Master.RightPlayer, PrefabType.WaterSlime, new Vector3(3f, GROUND_HEIGHT, 0f), gameContext);
        uninitialized.getComponents().add(new TestMob(uninitialized));
        register(uninitialized);
        GameObject reachable = enemy(Master.RightPlayer, 9f, GROUND_HEIGHT);
        register(reachable);

        GameObject detected = new ClosestEnemyDetector(gameContext, TargetMask.ANY.bit).detect(self);

        assertThat(detected).isSameAs(reachable);
    }

    @Test
    void keepsGroundOnlyDetectorsUnchanged() {
        GameObject self = enemy(Master.LeftPlayer, 0f, GROUND_HEIGHT);
        GameObject air = enemy(Master.RightPlayer, 1f, AIR_HEIGHT);
        GameObject ground = enemy(Master.RightPlayer, 5f, GROUND_HEIGHT);
        register(air, ground);

        GameObject detected = new ClosestEnemyDetector(gameContext, TargetMask.GROUND.bit).detect(self);

        assertThat(detected).isSameAs(ground);
    }

    private void register(GameObject... gameObjects) {
        sessionData.gameObjects.addAll(java.util.Arrays.asList(gameObjects));
    }

    private GameObject enemy(Master master, float x, float y) {
        GameObject gameObject = new GameObject(master, PrefabType.WaterSlime, new Vector3(x, y, 0f), gameContext);
        gameObject.getComponents().add(new TestMob(gameObject));
        gameObject.setStatus(Status.Idle);
        return gameObject;
    }

    private GameObject enemyAt(float x) {
        GameObject enemy = mock(GameObject.class);
        when(enemy.getMaster()).thenReturn(Master.RightPlayer);
        when(enemy.getPosition()).thenReturn(new Vector3(x, 0f, 0f));
        when(enemy.getComponents()).thenReturn(List.of(mock(Mob.class)));
        when(enemy.isActive()).thenReturn(true);
        return enemy;
    }

    private static class TestMob extends Mob {
        private TestMob(GameObject gameObject) {
            super(gameObject, 10, 1f);
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
