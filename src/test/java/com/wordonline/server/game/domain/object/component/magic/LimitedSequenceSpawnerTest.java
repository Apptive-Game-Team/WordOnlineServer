package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LimitedSequenceSpawnerTest {

    private static final float SPAWN_INTERVAL_SEC = 2f;
    private static final float TICK_DELTA_SEC = 0.5f;

    @Test
    void spawnsOnStartConstructorSpawnsOnFirstUpdateThenWaitsAFullIntervalForTheNext() {
        GameContext gameContext = mock(GameContext.class);
        GameObject owner = new GameObject(Master.LeftPlayer, PrefabType.DimensionToad, Vector3.ZERO, gameContext);
        when(gameContext.getDeltaTime()).thenReturn(TICK_DELTA_SEC);
        clearInvocations(gameContext);

        LimitedSequenceSpawner spawner = new LimitedSequenceSpawner(
                owner, SPAWN_INTERVAL_SEC, 0, true, PrefabType.FireTadpole);

        spawner.update();
        verify(gameContext, times(1)).createGameObject(any(GameObject.class));

        // Interval is 2s and ticks are 0.5s: three more ticks (1.5s) must not
        // trigger a second spawn yet.
        spawner.update();
        spawner.update();
        spawner.update();
        verify(gameContext, times(1)).createGameObject(any(GameObject.class));

        // The fourth tick after the first spawn completes the 2s cooldown.
        spawner.update();
        verify(gameContext, times(2)).createGameObject(any(GameObject.class));
    }

    @Test
    void legacyConstructorStillWaitsAFullIntervalBeforeItsFirstSpawn() {
        GameContext gameContext = mock(GameContext.class);
        GameObject owner = new GameObject(Master.LeftPlayer, PrefabType.DimensionToad, Vector3.ZERO, gameContext);
        when(gameContext.getDeltaTime()).thenReturn(TICK_DELTA_SEC);
        clearInvocations(gameContext);

        LimitedSequenceSpawner spawner = new LimitedSequenceSpawner(
                owner, SPAWN_INTERVAL_SEC, 0, PrefabType.FireTadpole);

        // 1.5s elapsed across three ticks: still short of the 2s interval.
        spawner.update();
        spawner.update();
        spawner.update();
        verify(gameContext, never()).createGameObject(any(GameObject.class));

        // The fourth tick reaches the 2s interval and produces the first spawn.
        spawner.update();
        verify(gameContext, times(1)).createGameObject(any(GameObject.class));
    }
}
