package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.EffectApplyPolicy;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShockTrapDetectorTest {

    private static final float RADIUS = 3f;
    private static final float TRIGGER_DELAY = 2f;
    private static final float STUN_DURATION = 1.5f;
    private static final float ATTACK_INTERVAL = 4f;

    @Test
    void stunsTargetsStillInRangeWhenTriggerDelayElapses() {
        GameObject trap = mock(GameObject.class);
        GameContext gameContext = mock(GameContext.class);
        GameObject enemy = enemyOf(trap, gameContext);
        CommonEffectReceiver receiver = mock(CommonEffectReceiver.class);
        when(enemy.getComponent(CommonEffectReceiver.class)).thenReturn(receiver);
        when(gameContext.overlapSphereAll(trap, RADIUS)).thenReturn(List.of(enemy));
        when(gameContext.getDeltaTime()).thenReturn(TRIGGER_DELAY);

        ShockTrapDetector detector = new ShockTrapDetector(trap, RADIUS, TRIGGER_DELAY, STUN_DURATION, ATTACK_INTERVAL);

        detector.update(); // enemy first seen: countdown starts, does not consume time yet
        verify(receiver, never()).applyEffect(any(), any(), any(), anyFloat());

        detector.update(); // countdown elapses: enemy still in range gets stunned
        verify(receiver, times(1)).applyEffect(
                eq(StatusEffectKey.TrapStun_Receive),
                any(),
                eq(EffectApplyPolicy.REFRESH_DURATION),
                eq(STUN_DURATION));
    }

    @Test
    void doesNotStunATargetThatLeftBeforeTriggerDelayElapses() {
        GameObject trap = mock(GameObject.class);
        GameContext gameContext = mock(GameContext.class);
        GameObject enemy = enemyOf(trap, gameContext);
        CommonEffectReceiver receiver = mock(CommonEffectReceiver.class);
        when(enemy.getComponent(CommonEffectReceiver.class)).thenReturn(receiver);
        when(gameContext.getDeltaTime()).thenReturn(TRIGGER_DELAY);

        ShockTrapDetector detector = new ShockTrapDetector(trap, RADIUS, TRIGGER_DELAY, STUN_DURATION, ATTACK_INTERVAL);

        when(gameContext.overlapSphereAll(trap, RADIUS)).thenReturn(List.of(enemy));
        detector.update(); // enemy seen: countdown starts

        when(gameContext.overlapSphereAll(trap, RADIUS)).thenReturn(List.of()); // enemy left the radius
        detector.update(); // countdown elapses, but nobody remains inside

        verify(receiver, never()).applyEffect(any(), any(), any(), anyFloat());
    }

    @Test
    void doesNotTriggerAgainBeforeReloadingAndRearmsAfterAttackInterval() {
        GameObject trap = mock(GameObject.class);
        GameContext gameContext = mock(GameContext.class);
        GameObject enemy = enemyOf(trap, gameContext);
        CommonEffectReceiver receiver = mock(CommonEffectReceiver.class);
        when(enemy.getComponent(CommonEffectReceiver.class)).thenReturn(receiver);
        when(gameContext.overlapSphereAll(trap, RADIUS)).thenReturn(List.of(enemy));
        when(gameContext.getDeltaTime()).thenReturn(TRIGGER_DELAY);

        ShockTrapDetector detector = new ShockTrapDetector(trap, RADIUS, TRIGGER_DELAY, STUN_DURATION, ATTACK_INTERVAL);

        detector.update(); // enemy seen: countdown starts
        detector.update(); // countdown elapses: first stun
        verify(receiver, times(1)).applyEffect(
                eq(StatusEffectKey.TrapStun_Receive), any(), eq(EffectApplyPolicy.REFRESH_DURATION), eq(STUN_DURATION));

        // enemy is still sitting in range, but the trap must reload for ATTACK_INTERVAL seconds first
        detector.update(); // reloadCounter == TRIGGER_DELAY (< ATTACK_INTERVAL)
        verify(receiver, times(1)).applyEffect(
                eq(StatusEffectKey.TrapStun_Receive), any(), eq(EffectApplyPolicy.REFRESH_DURATION), eq(STUN_DURATION));

        detector.update(); // reloadCounter == 2 * TRIGGER_DELAY == ATTACK_INTERVAL: re-arms, no detection this tick
        detector.update(); // re-armed: enemy still present, countdown starts again
        detector.update(); // countdown elapses again: second stun
        verify(receiver, times(2)).applyEffect(
                eq(StatusEffectKey.TrapStun_Receive), any(), eq(EffectApplyPolicy.REFRESH_DURATION), eq(STUN_DURATION));
    }

    private GameObject enemyOf(GameObject trap, GameContext gameContext) {
        when(trap.getGameContext()).thenReturn(gameContext);
        when(trap.getMaster()).thenReturn(Master.LeftPlayer);

        GameObject enemy = mock(GameObject.class);
        when(enemy.getMaster()).thenReturn(Master.RightPlayer);
        when(enemy.isDying()).thenReturn(false);
        return enemy;
    }
}
