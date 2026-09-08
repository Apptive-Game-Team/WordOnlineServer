package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FireworkLauncherTest {

    private static final float ATTACK_INTERVAL_SEC = 2f;
    private static final float ATTACK_OFFSET = 3f;
    private static final float TICK_DELTA_SEC = 0.5f;
    private static final Vector3 TOWER_POSITION = new Vector3(1f, 0f, 2f);

    private GameObject towerFor(Master master) {
        return new GameObject(master, PrefabType.FireworkTower, TOWER_POSITION, mock(GameContext.class));
    }

    @Test
    void leftPlayerImpactsAttackOffsetToThePositiveX() {
        GameObject tower = towerFor(Master.LeftPlayer);
        GameContext gameContext = tower.getGameContext();
        when(gameContext.getDeltaTime()).thenReturn(ATTACK_INTERVAL_SEC);
        clearInvocations(gameContext);

        new FireworkLauncher(tower, ATTACK_INTERVAL_SEC, ATTACK_OFFSET).update();

        ArgumentCaptor<GameObject> spawned = ArgumentCaptor.forClass(GameObject.class);
        verify(gameContext, times(1)).createGameObject(spawned.capture());
        assertThat(spawned.getValue().getType()).isEqualTo(PrefabType.FireworkShell);
        assertThat(spawned.getValue().getMaster()).isEqualTo(Master.LeftPlayer);
        assertThat(spawned.getValue().getPosition()).isEqualTo(new Vector3(4f, 0f, 2f));
    }

    @Test
    void rightPlayerImpactsAttackOffsetToTheNegativeX() {
        GameObject tower = towerFor(Master.RightPlayer);
        GameContext gameContext = tower.getGameContext();
        when(gameContext.getDeltaTime()).thenReturn(ATTACK_INTERVAL_SEC);
        clearInvocations(gameContext);

        new FireworkLauncher(tower, ATTACK_INTERVAL_SEC, ATTACK_OFFSET).update();

        ArgumentCaptor<GameObject> spawned = ArgumentCaptor.forClass(GameObject.class);
        verify(gameContext, times(1)).createGameObject(spawned.capture());
        assertThat(spawned.getValue().getMaster()).isEqualTo(Master.RightPlayer);
        assertThat(spawned.getValue().getPosition()).isEqualTo(new Vector3(-2f, 0f, 2f));
    }

    @Test
    void masterNoneNeverLaunchesAShell() {
        GameObject tower = towerFor(Master.None);
        GameContext gameContext = tower.getGameContext();
        when(gameContext.getDeltaTime()).thenReturn(ATTACK_INTERVAL_SEC);
        clearInvocations(gameContext);

        new FireworkLauncher(tower, ATTACK_INTERVAL_SEC, ATTACK_OFFSET).update();

        verify(gameContext, never()).createGameObject(any(GameObject.class));
    }

    @Test
    void spawnsExactlyOneShellPerAttackInterval() {
        GameObject tower = towerFor(Master.LeftPlayer);
        GameContext gameContext = tower.getGameContext();
        when(gameContext.getDeltaTime()).thenReturn(TICK_DELTA_SEC);
        clearInvocations(gameContext);

        FireworkLauncher launcher = new FireworkLauncher(tower, ATTACK_INTERVAL_SEC, ATTACK_OFFSET);

        // Interval is 2s and ticks are 0.5s: three ticks (1.5s) must not fire yet.
        launcher.update();
        launcher.update();
        launcher.update();
        verify(gameContext, never()).createGameObject(any(GameObject.class));

        // The fourth tick completes the 2s cooldown: exactly one shell.
        launcher.update();
        verify(gameContext, times(1)).createGameObject(any(GameObject.class));

        // Another 1.5s must not produce a second shell yet.
        launcher.update();
        launcher.update();
        launcher.update();
        verify(gameContext, times(1)).createGameObject(any(GameObject.class));

        // Completing the next 2s window produces exactly one more shell.
        launcher.update();
        verify(gameContext, times(2)).createGameObject(any(GameObject.class));
    }
}
