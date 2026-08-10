package com.wordonline.server.game.domain.object.component.magic;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VineHitTrackerTest {

    @Test
    void countsEachDamagedObjectOnlyOnce() {
        VineHitTracker tracker = new VineHitTracker();

        assertThat(tracker.markIfFirstHit(10)).isTrue();
        assertThat(tracker.markIfFirstHit(10)).isFalse();
        assertThat(tracker.markIfFirstHit(20)).isTrue();
        assertThat(tracker.hitCount()).isEqualTo(2);
    }
}
