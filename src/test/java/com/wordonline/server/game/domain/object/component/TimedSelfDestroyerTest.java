package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.frame.GaugeCategory;
import com.wordonline.server.game.dto.frame.GaugeDto;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TimedSelfDestroyerTest {

    private final GameObject gameObject = mock(GameObject.class);
    private final GameContext gameContext = mock(GameContext.class);

    private TimedSelfDestroyer destroyer(float timeToLive) {
        when(gameObject.getGameContext()).thenReturn(gameContext);
        return new TimedSelfDestroyer(gameObject, timeToLive);
    }

    // --- pre-existing behavior, pinned before the per-frame recover() cap is added ---

    @Test
    void doesNotDestroyBeforeItsTimeToLiveElapses() {
        when(gameContext.getDeltaTime()).thenReturn(1f);
        TimedSelfDestroyer destroyer = destroyer(3f);

        destroyer.update();
        destroyer.update();

        verify(gameObject, never()).destroy();
    }

    @Test
    void destroysOnceElapsedTimeReachesTimeToLive() {
        when(gameContext.getDeltaTime()).thenReturn(1f);
        TimedSelfDestroyer destroyer = destroyer(3f);

        destroyer.update();
        destroyer.update();
        destroyer.update();

        verify(gameObject).destroy();
    }

    @Test
    void recoverWithNoArgumentsResetsElapsedTimeToZero() {
        when(gameContext.getDeltaTime()).thenReturn(1f);
        TimedSelfDestroyer destroyer = destroyer(3f);

        destroyer.update();
        destroyer.update();
        destroyer.recover();

        GaugeDto gauge = destroyer.getGauge();
        assertThat(gauge.value()).isEqualTo(3f);
        verify(gameObject).applyUpdate();
    }

    @Test
    void recoverWithAmountSubtractsFromElapsedTimeAndClampsAtZero() {
        when(gameContext.getDeltaTime()).thenReturn(1f);
        TimedSelfDestroyer destroyer = destroyer(10f);

        destroyer.update(); // elapsedTime = 1

        destroyer.recover(0.4f);
        assertThat(destroyer.getGauge().value()).isEqualTo(10f - 0.6f);

        destroyer.recover(100f);
        assertThat(destroyer.getGauge().value()).isEqualTo(10f);

        verify(gameObject, org.mockito.Mockito.times(2)).applyUpdate();
    }

    @Test
    void recoverWithNonPositiveAmountIsANoOp() {
        when(gameContext.getDeltaTime()).thenReturn(1f);
        TimedSelfDestroyer destroyer = destroyer(10f);
        destroyer.update(); // elapsedTime = 1

        destroyer.recover(0f);
        destroyer.recover(-5f);

        assertThat(destroyer.getGauge().value()).isEqualTo(9f);
        verify(gameObject, never()).applyUpdate();
    }

    @Test
    void gaugeReportsRemainingTimeAgainstTimeToLiveAsTtlCategory() {
        when(gameContext.getDeltaTime()).thenReturn(2f);
        TimedSelfDestroyer destroyer = destroyer(5f);

        destroyer.update();

        GaugeDto gauge = destroyer.getGauge();
        assertThat(gauge.value()).isEqualTo(3f);
        assertThat(gauge.maxValue()).isEqualTo(5f);
        assertThat(gauge.category()).isEqualTo(GaugeCategory.TTL);
    }

    // --- new behavior: recover(amount) is capped to at most one tick of rewind per frame ---

    @Test
    void recoverAmountIsCappedToOneTickPerFrameEvenWhenCalledRepeatedly() {
        when(gameContext.getDeltaTime()).thenReturn(1f);
        TimedSelfDestroyer destroyer = destroyer(100f);

        for (int i = 0; i < 5; i++) {
            destroyer.update(); // elapsedTime = 5, away from the zero floor
        }
        destroyer.update(); // elapsedTime = 6, one tick of decay this frame

        // three overlapping repair auras each try to recover a full tick in the same frame
        destroyer.recover(1f);
        destroyer.recover(1f);
        destroyer.recover(1f);

        // only one tick's worth is actually rewound: the totem freezes the timer for this frame,
        // it does not run it backwards just because several auras overlap it
        assertThat(destroyer.getGauge().value()).isEqualTo(95f);
    }

    @Test
    void recoverAmountCapResetsEveryFrame() {
        when(gameContext.getDeltaTime()).thenReturn(1f);
        TimedSelfDestroyer destroyer = destroyer(100f);

        for (int i = 0; i < 5; i++) {
            destroyer.update();
        }
        destroyer.update(); // elapsedTime = 6
        destroyer.recover(1f);
        destroyer.recover(1f); // blocked by this frame's cap; elapsedTime stays at 5

        destroyer.update(); // next frame: cap resets, elapsedTime = 5 + 1 = 6
        destroyer.recover(1f); // honored again now that the cap has reset

        assertThat(destroyer.getGauge().value()).isEqualTo(95f);
    }

    @Test
    void withoutAnyCallerOfRecoverAmountTheCapHasNoEffectOnPlainDecay() {
        when(gameContext.getDeltaTime()).thenReturn(1f);
        TimedSelfDestroyer destroyer = destroyer(3f);

        destroyer.update();
        destroyer.update();
        destroyer.update();

        verify(gameObject).destroy();
    }
}
