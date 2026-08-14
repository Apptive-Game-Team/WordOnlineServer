package com.wordonline.server.statistic.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.wordonline.server.statistic.dto.GameFrameSummaryDto;
import com.wordonline.server.statistic.dto.GameTimingDetailDto;
import com.wordonline.server.statistic.dto.PageDto;
import com.wordonline.server.statistic.dto.PerformanceFilterDto;
import com.wordonline.server.statistic.dto.SystemTimingDto;
import com.wordonline.server.statistic.dto.TimeSeriesPointDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Exercises the read queries against a real in-memory database, so the SQL itself is under test
 * rather than a mock of it.
 */
class StatisticPerformanceRepositoryTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 8, 1, 12, 0);

    private static StatisticPerformanceRepository repository;
    private static JdbcClient jdbcClient;

    @BeforeAll
    static void setUp() throws Exception {
        jdbcClient = StatisticSchemaFixture.createStatisticTables("statisticPerformanceRepositoryTest");
        repository = new StatisticPerformanceRepository(jdbcClient);

        // Five PVP games whose Frame means are 10..50ms, so the median is 30ms and p95 is 48ms.
        for (int i = 1; i <= 5; i++) {
            long gameId = insertGame(BASE.plusDays(i), "PVP", 300 + i);
            insertTiming(gameId, "Frame", 5_000_000L * i, 90_000_000L + i, 10_000_000.0 * i);
            insertTiming(gameId, "PhysicSystem", 1_000_000L, 4_000_000L, 2_000_000.0);
        }
        // A synthetic bot game, far slower, to prove the game type filter excludes it.
        long practiceId = insertGame(BASE.plusDays(3), "Practice", 60);
        insertTiming(practiceId, "Frame", 400_000_000L, 900_000_000L, 500_000_000.0);
        // A game with no Frame row at all, to prove the recent games left join keeps it.
        insertGame(BASE.plusDays(6), "PVP", 120);
    }

    private static long insertGame(LocalDateTime createdAt, String gameType, long duration) {
        jdbcClient.sql("""
                INSERT INTO statistic_games(win_user_id, loss_user_id, duration, created_at, game_type)
                VALUES(1, 2, :duration, :createdAt, :gameType)
                """)
                .param("duration", duration)
                .param("createdAt", createdAt)
                .param("gameType", gameType)
                .update();
        return jdbcClient.sql("SELECT MAX(id) FROM statistic_games").query(Long.class).single();
    }

    private static void insertTiming(long gameId, String name, long min, long max, double mean) {
        jdbcClient.sql("""
                INSERT INTO statistic_update_time(statistic_game_id, name, min_interval_ns, max_interval_ns, mean_interval_ns)
                VALUES(:gameId, :name, :min, :max, :mean)
                """)
                .param("gameId", gameId)
                .param("name", name)
                .param("min", min)
                .param("max", max)
                .param("mean", mean)
                .update();
    }

    private SystemTimingDto timingNamed(List<SystemTimingDto> timings, String name) {
        return timings.stream().filter(t -> t.name().equals(name)).findFirst().orElseThrow();
    }

    @Test
    void reportsMedianAndPercentileOfPerGameMeansRatherThanAnAverage() {
        List<SystemTimingDto> timings = repository.findSystemTimings(
                new PerformanceFilterDto(null, null, "PVP"));

        SystemTimingDto frame = timingNamed(timings, "Frame");
        assertThat(frame.gameCount()).isEqualTo(5);
        // Means are 10,20,30,40,50ms: median 30ms, p95 interpolates to 48ms.
        assertThat(frame.medianMeanIntervalNs()).isCloseTo(30_000_000.0, within(50_000.0));
        assertThat(frame.p95MeanIntervalNs()).isCloseTo(48_000_000.0, within(50_000.0));
        // Extremes come straight from the per-row min and max, not from the means.
        assertThat(frame.minIntervalNs()).isEqualTo(5_000_000L);
        assertThat(frame.maxIntervalNs()).isEqualTo(90_000_005L);
    }

    @Test
    void sortsSlowestFirst() {
        List<SystemTimingDto> timings = repository.findSystemTimings(
                new PerformanceFilterDto(null, null, "PVP"));

        assertThat(timings).extracting(SystemTimingDto::name)
                .containsExactly("Frame", "PhysicSystem");
    }

    @Test
    void gameTypeFilterKeepsSyntheticBotGamesOutOfTheAggregate() {
        SystemTimingDto pvpOnly = timingNamed(
                repository.findSystemTimings(new PerformanceFilterDto(null, null, "PVP")), "Frame");
        SystemTimingDto everything = timingNamed(
                repository.findSystemTimings(PerformanceFilterDto.all()), "Frame");

        assertThat(pvpOnly.gameCount()).isEqualTo(5);
        assertThat(everything.gameCount()).isEqualTo(6);
        // The Practice game's 500ms mean would badly skew the unfiltered view.
        assertThat(everything.maxIntervalNs()).isEqualTo(900_000_000L);
        assertThat(pvpOnly.maxIntervalNs()).isEqualTo(90_000_005L);
    }

    @Test
    void timeRangeFilterIsAppliedToGameCreatedAt() {
        List<SystemTimingDto> timings = repository.findSystemTimings(
                new PerformanceFilterDto(BASE.plusDays(2), BASE.plusDays(4), "PVP"));

        // Inclusive lower bound, exclusive upper bound: days 2 and 3 only.
        assertThat(timingNamed(timings, "Frame").gameCount()).isEqualTo(2);
    }

    @Test
    void timeSeriesReturnsPerGameMeansInChronologicalOrder() {
        List<TimeSeriesPointDto> series = repository.findTimeSeries(
                "Frame", new PerformanceFilterDto(null, null, "PVP"));

        assertThat(series).hasSize(5);
        assertThat(series).extracting(TimeSeriesPointDto::createdAt).isSorted();
        assertThat(series.getFirst().meanIntervalNs()).isCloseTo(10_000_000.0, within(50_000.0));
        assertThat(series.getLast().meanIntervalNs()).isCloseTo(50_000_000.0, within(50_000.0));
    }

    @Test
    void recentGamesArePaginatedNewestFirstAndCarryTheirFrameMean() {
        PageDto<GameFrameSummaryDto> first = repository.findRecentGames(
                new PerformanceFilterDto(null, null, "PVP"), 0, 2);

        assertThat(first.totalCount()).isEqualTo(6);
        assertThat(first.totalPages()).isEqualTo(3);
        assertThat(first.content()).hasSize(2);
        assertThat(first.content()).extracting(GameFrameSummaryDto::createdAt).isSortedAccordingTo(
                java.util.Comparator.reverseOrder());
        // Newest PVP game has no Frame row; the left join keeps it with null timings.
        assertThat(first.content().getFirst().frameMeanIntervalNs()).isNull();
        assertThat(first.content().getFirst().frameMaxIntervalNs()).isNull();

        PageDto<GameFrameSummaryDto> second = repository.findRecentGames(
                new PerformanceFilterDto(null, null, "PVP"), 1, 2);
        assertThat(second.content()).extracting(GameFrameSummaryDto::gameId)
                .doesNotContainAnyElementsOf(first.content().stream().map(GameFrameSummaryDto::gameId).toList());
        assertThat(second.content().getFirst().frameMeanIntervalNs()).isNotNull();
    }

    @Test
    void gameDetailReturnsEveryMeasuredNameForThatGameOnly() {
        long gameId = repository.findRecentGames(new PerformanceFilterDto(null, null, "PVP"), 0, 20)
                .content().stream()
                .filter(game -> game.frameMeanIntervalNs() != null)
                .findFirst()
                .orElseThrow()
                .gameId();

        List<GameTimingDetailDto> timings = repository.findGameTimings(gameId);

        assertThat(timings).extracting(GameTimingDetailDto::name)
                .containsExactlyInAnyOrder("Frame", "PhysicSystem");
    }

    @Test
    void findGameReturnsEmptyForAnUnknownId() {
        assertThat(repository.findGame(999_999L)).isEmpty();
        assertThat(repository.findGameTimings(999_999L)).isEmpty();
    }

    @Test
    void findGameTypesListsTheTypesActuallyRecorded() {
        assertThat(repository.findGameTypes()).containsExactly("PVP", "Practice");
    }
}
