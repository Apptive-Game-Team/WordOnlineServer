package com.wordonline.server.statistic.domain;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.statistic.dto.GameResultDto;

import static org.assertj.core.api.Assertions.assertThat;

class GameResultBuilderTest {

    private static final long FIFTY_MS_NS = 50_000_000L;

    @Test
    void recordsNothingForTheFirstFrameBecauseThereIsNoPrecedingFrame() {
        GameResultBuilder builder = new GameResultBuilder();

        builder.recordFrameStart(1_000L);

        assertThat(statisticsOf(builder)).isEmpty();
    }

    @Test
    void recordsTheGapBetweenConsecutiveFramesUnderTheFrameKey() {
        GameResultBuilder builder = new GameResultBuilder();

        builder.recordFrameStart(0L);
        builder.recordFrameStart(FIFTY_MS_NS);

        Map<String, UpdateTimeStatistic> statistics = statisticsOf(builder);
        assertThat(statistics).containsOnlyKeys(GameResultBuilder.FRAME_STATISTIC_NAME);
        UpdateTimeStatistic frame = statistics.get(GameResultBuilder.FRAME_STATISTIC_NAME);
        assertThat(frame.getFrameNum()).isEqualTo(1);
        assertThat(frame.getMinInterval()).isEqualTo(FIFTY_MS_NS);
        assertThat(frame.getMaxInterval()).isEqualTo(FIFTY_MS_NS);
    }

    @Test
    void aggregatesEveryLaterFrameSoADegradedFrameRateShowsUpInTheMaximum() {
        GameResultBuilder builder = new GameResultBuilder();

        long now = 0L;
        builder.recordFrameStart(now);
        builder.recordFrameStart(now += FIFTY_MS_NS);
        // A frame that started a full second late: the axis per-system durations cannot show.
        builder.recordFrameStart(now += 1_000_000_000L);
        builder.recordFrameStart(now + FIFTY_MS_NS);

        UpdateTimeStatistic frame = statisticsOf(builder).get(GameResultBuilder.FRAME_STATISTIC_NAME);
        assertThat(frame.getFrameNum()).isEqualTo(3);
        assertThat(frame.getMinInterval()).isEqualTo(FIFTY_MS_NS);
        assertThat(frame.getMaxInterval()).isEqualTo(1_000_000_000L);
    }

    @Test
    void keepsFrameIntervalsSeparateFromPerSystemDurations() {
        GameResultBuilder builder = new GameResultBuilder();

        builder.recordFrameStart(0L);
        builder.recordFrameStart(FIFTY_MS_NS);
        builder.addInterval("PhysicSystem", 1_234L);

        assertThat(statisticsOf(builder))
                .containsOnlyKeys(GameResultBuilder.FRAME_STATISTIC_NAME, "PhysicSystem");
    }

    private Map<String, UpdateTimeStatistic> statisticsOf(GameResultBuilder builder) {
        GameResultDto dto = builder.build(Master.RightPlayer, SessionType.PVP);
        assertThat(dto).isNotNull();
        return dto.updateTimeStatisticMap();
    }
}
