package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GrassSpreadTest {

    private static final float ATTACK_INTERVAL_SEC = 2f;
    private static final float RADIUS = 5f;
    private static final int QUANTITY_PER_RING = 4;
    private static final float TICK_DELTA_SEC = 0.5f;

    @Test
    void spreadsExactlyOneLeafFieldPerAttackInterval() {
        GameContext gameContext = mock(GameContext.class);
        GameObject building = new GameObject(Master.LeftPlayer, PrefabType.GrassGenerator, Vector3.ZERO, gameContext);
        when(gameContext.getDeltaTime()).thenReturn(TICK_DELTA_SEC);
        clearInvocations(gameContext);

        GrassSpread grassSpread = new GrassSpread(building, ATTACK_INTERVAL_SEC, RADIUS, QUANTITY_PER_RING);

        // 1.5s across three ticks: short of the 2s interval, nothing spreads yet.
        grassSpread.update();
        grassSpread.update();
        grassSpread.update();
        verify(gameContext, never()).createGameObject(any(GameObject.class));

        // The fourth tick reaches the 2s interval: exactly one field appears.
        grassSpread.update();
        verify(gameContext, times(1)).createGameObject(any(GameObject.class));

        // Three more ticks (1.5s) must not add a second field yet.
        grassSpread.update();
        grassSpread.update();
        grassSpread.update();
        verify(gameContext, times(1)).createGameObject(any(GameObject.class));

        // The next tick completes the second interval.
        grassSpread.update();
        verify(gameContext, times(2)).createGameObject(any(GameObject.class));
    }

    @Test
    void aStalledFrameCatchesUpInsteadOfDroppingSpreads() {
        GameContext gameContext = mock(GameContext.class);
        GameObject building = new GameObject(Master.LeftPlayer, PrefabType.GrassGenerator, Vector3.ZERO, gameContext);
        // one frame that took 3.5 intervals worth of time to complete
        when(gameContext.getDeltaTime()).thenReturn(ATTACK_INTERVAL_SEC * 3.5f);
        clearInvocations(gameContext);

        GrassSpread grassSpread = new GrassSpread(building, ATTACK_INTERVAL_SEC, RADIUS, QUANTITY_PER_RING);
        grassSpread.update();

        verify(gameContext, times(3)).createGameObject(any(GameObject.class));
    }

    @Test
    void everySpreadPositionStaysWithinRadiusOfTheBuildingAsLeafField() {
        GameContext gameContext = mock(GameContext.class);
        Vector3 center = new Vector3(10f, 0f, -3f);
        GameObject building = new GameObject(Master.LeftPlayer, PrefabType.GrassGenerator, center, gameContext);
        when(gameContext.getDeltaTime()).thenReturn(ATTACK_INTERVAL_SEC);
        clearInvocations(gameContext);

        GrassSpread grassSpread = new GrassSpread(building, ATTACK_INTERVAL_SEC, RADIUS, QUANTITY_PER_RING);
        for (int i = 0; i < 12; i++) {
            grassSpread.update();
        }

        ArgumentCaptor<GameObject> spawned = ArgumentCaptor.forClass(GameObject.class);
        verify(gameContext, times(12)).createGameObject(spawned.capture());

        List<GameObject> fields = spawned.getAllValues();
        assertThat(fields).hasSize(12);
        for (GameObject field : fields) {
            assertThat(field.getType()).isEqualTo(PrefabType.LeafField);

            Vector3 position = field.getPosition();
            float dx = position.getX() - center.getX();
            float dz = position.getZ() - center.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            assertThat(distance).isLessThanOrEqualTo(RADIUS + 0.01);
        }
    }

    @Test
    void stopsSpreadingOnceTheBuildingIsDestroyed() {
        GameContext gameContext = mock(GameContext.class);
        GameObject building = new GameObject(Master.LeftPlayer, PrefabType.GrassGenerator, Vector3.ZERO, gameContext);
        when(gameContext.getDeltaTime()).thenReturn(ATTACK_INTERVAL_SEC);
        clearInvocations(gameContext);

        GrassSpread grassSpread = new GrassSpread(building, ATTACK_INTERVAL_SEC, RADIUS, QUANTITY_PER_RING);
        grassSpread.update();
        verify(gameContext, times(1)).createGameObject(any(GameObject.class));

        building.destroy();
        clearInvocations(gameContext);

        grassSpread.update();
        grassSpread.update();
        verify(gameContext, never()).createGameObject(any(GameObject.class));
    }

    @Test
    void stopsSpreadingWhileACapWorthOfFieldsIsStillAlive() {
        GameContext gameContext = mock(GameContext.class);
        GameObject building = new GameObject(Master.LeftPlayer, PrefabType.GrassGenerator, Vector3.ZERO, gameContext);
        when(gameContext.getDeltaTime()).thenReturn(ATTACK_INTERVAL_SEC * (GrassSpread.MAX_ALIVE_FIELD_COUNT + 5));
        clearInvocations(gameContext);

        GrassSpread grassSpread = new GrassSpread(building, ATTACK_INTERVAL_SEC, RADIUS, QUANTITY_PER_RING);
        grassSpread.update();
        grassSpread.update();

        // no field has expired yet, so the cap is the only thing holding the count down
        verify(gameContext, times(GrassSpread.MAX_ALIVE_FIELD_COUNT)).createGameObject(any(GameObject.class));
    }

    @Test
    void spreadsAgainOnceExpiredFieldsFreeTheirSlots() {
        GameContext gameContext = mock(GameContext.class);
        GameObject building = new GameObject(Master.LeftPlayer, PrefabType.GrassGenerator, Vector3.ZERO, gameContext);
        when(gameContext.getDeltaTime()).thenReturn(ATTACK_INTERVAL_SEC * (GrassSpread.MAX_ALIVE_FIELD_COUNT + 5));
        clearInvocations(gameContext);

        GrassSpread grassSpread = new GrassSpread(building, ATTACK_INTERVAL_SEC, RADIUS, QUANTITY_PER_RING);
        grassSpread.update();

        ArgumentCaptor<GameObject> spawned = ArgumentCaptor.forClass(GameObject.class);
        verify(gameContext, times(GrassSpread.MAX_ALIVE_FIELD_COUNT)).createGameObject(spawned.capture());
        List<GameObject> firstFields = spawned.getAllValues();
        clearInvocations(gameContext);

        // every leaf field runs out its own TimedSelfDestroyer duration
        firstFields.forEach(GameObject::destroy);

        when(gameContext.getDeltaTime()).thenReturn(ATTACK_INTERVAL_SEC * 3);
        grassSpread.update();

        verify(gameContext, times(3)).createGameObject(any(GameObject.class));
    }
}
