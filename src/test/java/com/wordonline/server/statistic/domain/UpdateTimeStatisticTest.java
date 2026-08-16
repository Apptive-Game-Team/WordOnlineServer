package com.wordonline.server.statistic.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateTimeStatisticTest {

    @Test
    void recordsIntervalsLargerThanIntegerMaxValueWithoutOverflowing() {
        // A stalled frame can easily exceed the ~2.147s an int of nanoseconds can hold.
        long fourSecondsNs = 4_000_000_000L;
        UpdateTimeStatistic statistic = new UpdateTimeStatistic();

        statistic.addInterval(fourSecondsNs);

        assertThat(statistic.getMinInterval()).isEqualTo(fourSecondsNs);
        assertThat(statistic.getMaxInterval()).isEqualTo(fourSecondsNs);
        assertThat(statistic.getMeanInterval()).isEqualTo((float) fourSecondsNs);
    }

    @Test
    void keepsMinMaxAndMeanAcrossSeveralSamples() {
        UpdateTimeStatistic statistic = new UpdateTimeStatistic();

        statistic.addInterval(10L);
        statistic.addInterval(60L);
        statistic.addInterval(20L);

        assertThat(statistic.getFrameNum()).isEqualTo(3);
        assertThat(statistic.getMinInterval()).isEqualTo(10L);
        assertThat(statistic.getMaxInterval()).isEqualTo(60L);
        assertThat(statistic.getMeanInterval()).isEqualTo(30.0f);
    }

    @Test
    void doesNotLetTheIntSeededBoundsClampALargeFirstSample() {
        // Regression guard: seeding min/max from Integer.MAX_VALUE/MIN_VALUE would silently
        // cap the very first frame interval once the fields widened to long.
        UpdateTimeStatistic statistic = new UpdateTimeStatistic();

        statistic.addInterval(Integer.MAX_VALUE + 1L);

        assertThat(statistic.getMaxInterval()).isGreaterThan(Integer.MAX_VALUE);
        assertThat(statistic.getMinInterval()).isEqualTo(statistic.getMaxInterval());
    }
}
