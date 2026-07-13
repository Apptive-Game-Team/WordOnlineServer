package com.wordonline.server.game.service.lockstep;

import com.wordonline.server.game.dto.lockstep.FrameInputDto;
import com.wordonline.server.game.dto.lockstep.FrameSubmissionDto;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LockstepFrameBufferTest {
    @Test
    void canonicalizesInputsByUserThenSequence() throws Exception {
        LockstepFrameBuffer buffer = new LockstepFrameBuffer(1, 1, 2, Set.of(20L, 10L));
        buffer.submit(20L, submission(1, "same", input(8), input(2)));
        buffer.submit(10L, submission(1, "same", input(7), input(1)));

        FrameResolution resolution = buffer.awaitCurrentFrame(Duration.ZERO);

        assertThat(resolution.complete()).isTrue();
        assertThat(resolution.hashMatched()).isTrue();
        assertThat(resolution.inputs())
                .extracting(value -> value.userId() + ":" + value.input().sequence())
                .containsExactly("10:1", "10:7", "20:2", "20:8");
    }

    @Test
    void acceptsIdenticalRetryButRejectsConflictingDuplicate() {
        LockstepFrameBuffer buffer = new LockstepFrameBuffer(1, 1, 2, Set.of(10L));
        FrameSubmissionDto original = submission(1, "hash", input(1));
        buffer.submit(10L, original);
        buffer.submit(10L, original);

        assertThatThrownBy(() -> buffer.submit(10L, submission(1, "other", input(1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Conflicting duplicate");
    }

    @Test
    void detectsPeerHashMismatchWithoutChoosingAnAuthority() throws Exception {
        LockstepFrameBuffer buffer = new LockstepFrameBuffer(1, 1, 2, Set.of(10L, 20L));
        buffer.submit(10L, submission(1, "aaa", input(1)));
        buffer.submit(20L, submission(1, "bbb", input(1)));

        FrameResolution resolution = buffer.awaitCurrentFrame(Duration.ZERO);

        assertThat(resolution.complete()).isTrue();
        assertThat(resolution.hashMatched()).isFalse();
        assertThat(resolution.hashes()).containsEntry(10L, "aaa").containsEntry(20L, "bbb");
    }

    @Test
    void reportsMissingParticipantOnTimeout() throws Exception {
        LockstepFrameBuffer buffer = new LockstepFrameBuffer(1, 1, 2, Set.of(10L, 20L));
        buffer.submit(10L, submission(1, "hash", input(1)));

        FrameResolution resolution = buffer.awaitCurrentFrame(Duration.ZERO);

        assertThat(resolution.complete()).isFalse();
        assertThat(resolution.missingParticipantIds()).containsExactly(20L);
    }

    @Test
    void soloSessionDoesNotWaitForPeerHash() throws Exception {
        LockstepFrameBuffer buffer = new LockstepFrameBuffer(1, 1, 2, Set.of(10L));
        buffer.submit(10L, submission(1, "solo", input(1)));

        FrameResolution resolution = buffer.awaitCurrentFrame(Duration.ZERO);

        assertThat(resolution.complete()).isTrue();
        assertThat(resolution.hashMatched()).isTrue();
    }

    @Test
    void rejectsLateFarFutureWrongProtocolAndDuplicateSequence() throws Exception {
        LockstepFrameBuffer buffer = new LockstepFrameBuffer(1, 1, 2, Set.of(10L));

        assertThatThrownBy(() -> buffer.submit(10L, new FrameSubmissionDto(2, 1, "hash", List.of())))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("protocol");
        assertThatThrownBy(() -> buffer.submit(10L, submission(4, "hash", input(1))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("future window");
        assertThatThrownBy(() -> buffer.submit(10L, submission(1, "hash", input(1), input(1))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Duplicate input sequence");

        buffer.submit(10L, submission(1, "hash", input(1)));
        buffer.awaitCurrentFrame(Duration.ZERO);
        assertThatThrownBy(() -> buffer.submit(10L, submission(1, "hash", input(1))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Late frame");
    }

    private static FrameSubmissionDto submission(int frame, String hash, FrameInputDto... inputs) {
        return new FrameSubmissionDto(1, frame, hash, List.of(inputs));
    }

    private static FrameInputDto input(int sequence) {
        return new FrameInputDto(sequence, "useMagic", sequence, List.of(), null);
    }
}
