package com.wordonline.server.game.service.lockstep;

import com.wordonline.server.game.dto.lockstep.ClientReadyDto;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LockstepReadyBarrierTest {
    private final LockstepReadyBarrier barrier = new LockstepReadyBarrier(1, "sim-1", "config-1", Set.of(10L, 20L));

    @Test
    void completesOnlyAfterEveryParticipantReportsExactVersions() throws Exception {
        barrier.ready(10L, ready());
        assertThat(barrier.awaitReady(Duration.ZERO)).containsExactly(20L);

        barrier.ready(20L, ready());
        assertThat(barrier.awaitReady(Duration.ZERO)).isEmpty();
    }

    @Test
    void rejectsVersionMismatchBeforeSessionStart() {
        assertThatThrownBy(() -> barrier.ready(10L, new ClientReadyDto(1, "other", "config-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Lockstep client version mismatch");
    }

    private static ClientReadyDto ready() {
        return new ClientReadyDto(1, "sim-1", "config-1");
    }
}
