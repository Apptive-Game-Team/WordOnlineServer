package com.wordonline.server.statistic.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.wordonline.server.statistic.domain.GameResultBuilder;
import com.wordonline.server.statistic.dto.GameFrameSummaryDto;
import com.wordonline.server.statistic.dto.GameTimingDetailDto;
import com.wordonline.server.statistic.dto.PageDto;
import com.wordonline.server.statistic.dto.PerformanceFilterDto;
import com.wordonline.server.statistic.dto.SystemTimingDto;
import com.wordonline.server.statistic.dto.TimeSeriesPointDto;

import lombok.RequiredArgsConstructor;

/**
 * Read side of the timing statistics. {@link StatisticRepository} only writes; nothing until now
 * read these tables back, which is why frame timing regressions were invisible outside psql.
 * <p>
 * Every parameter is wrapped in an explicit {@code CAST}. Postgres cannot infer the type of a bare
 * parameter in {@code ? IS NULL}, so the optional-filter idiom below does not work without it.
 * {@code game_type} is cast to text because it is a Postgres enum in production but a plain
 * {@code VARCHAR} in the H2 test schema; the cast is what lets one statement serve both.
 */
@Repository
@RequiredArgsConstructor
public class StatisticPerformanceRepository {

    private final JdbcClient jdbcClient;

    /** Shared filter predicate. Every query joins {@code statistic_games} as {@code g}. */
    private static final String FILTER = """
            AND (CAST(:from AS TIMESTAMP) IS NULL OR g.created_at >= CAST(:from AS TIMESTAMP))
            AND (CAST(:to AS TIMESTAMP) IS NULL OR g.created_at < CAST(:to AS TIMESTAMP))
            AND (CAST(:gameType AS VARCHAR) IS NULL OR g.game_type::text = CAST(:gameType AS VARCHAR))
            """;

    // Percentiles of the per-game means rather than AVG(mean_interval_ns): see SystemTimingDto.
    //
    // mean_interval_ns is REAL, and the cast to DECIMAL is load bearing. An interpolating percentile
    // over large float values makes H2 derive a negative result scale and reject the statement
    // outright ("Scale ... must be between 0 and 100000"); casting to a type with a declared scale
    // avoids it. Postgres computes the same value either way, so one statement still serves both.
    private static final String FIND_SYSTEM_TIMINGS = """
            SELECT ut.name AS name,
                   MIN(ut.min_interval_ns) AS min_interval_ns,
                   MAX(ut.max_interval_ns) AS max_interval_ns,
                   PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY CAST(ut.mean_interval_ns AS DECIMAL(30, 3)))
                       AS median_mean_interval_ns,
                   PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY CAST(ut.mean_interval_ns AS DECIMAL(30, 3)))
                       AS p95_mean_interval_ns,
                   COUNT(*) AS game_count
            FROM statistic_update_time ut
            JOIN statistic_games g ON g.id = ut.statistic_game_id
            WHERE 1 = 1
            """ + FILTER + """
            GROUP BY ut.name
            ORDER BY p95_mean_interval_ns DESC, ut.name ASC
            """;

    private static final String FIND_TIME_SERIES = """
            SELECT g.id AS game_id,
                   g.created_at AS created_at,
                   ut.mean_interval_ns AS mean_interval_ns
            FROM statistic_update_time ut
            JOIN statistic_games g ON g.id = ut.statistic_game_id
            WHERE ut.name = CAST(:name AS VARCHAR)
            """ + FILTER + """
            ORDER BY g.created_at ASC, g.id ASC
            """;

    // Left join: a game with no Frame row still lists, with null timings.
    private static final String FIND_RECENT_GAMES = """
            SELECT g.id AS game_id,
                   g.created_at AS created_at,
                   g.game_type::text AS game_type,
                   g.duration AS duration,
                   f.mean_interval_ns AS frame_mean_interval_ns,
                   f.max_interval_ns AS frame_max_interval_ns
            FROM statistic_games g
            LEFT JOIN statistic_update_time f
                   ON f.statistic_game_id = g.id AND f.name = CAST(:frameName AS VARCHAR)
            WHERE 1 = 1
            """ + FILTER + """
            ORDER BY g.created_at DESC, g.id DESC
            LIMIT CAST(:limit AS INTEGER) OFFSET CAST(:offset AS INTEGER)
            """;

    private static final String COUNT_RECENT_GAMES = """
            SELECT COUNT(*) AS total
            FROM statistic_games g
            WHERE 1 = 1
            """ + FILTER;

    private static final String FIND_GAME_TIMINGS = """
            SELECT ut.name AS name,
                   ut.min_interval_ns AS min_interval_ns,
                   ut.max_interval_ns AS max_interval_ns,
                   ut.mean_interval_ns AS mean_interval_ns
            FROM statistic_update_time ut
            WHERE ut.statistic_game_id = CAST(:gameId AS BIGINT)
            ORDER BY ut.mean_interval_ns DESC, ut.name ASC
            """;

    private static final String FIND_GAME = """
            SELECT g.id AS game_id,
                   g.created_at AS created_at,
                   g.game_type::text AS game_type,
                   g.duration AS duration,
                   f.mean_interval_ns AS frame_mean_interval_ns,
                   f.max_interval_ns AS frame_max_interval_ns
            FROM statistic_games g
            LEFT JOIN statistic_update_time f
                   ON f.statistic_game_id = g.id AND f.name = CAST(:frameName AS VARCHAR)
            WHERE g.id = CAST(:gameId AS BIGINT)
            """;

    private static final String FIND_GAME_TYPES = """
            SELECT DISTINCT g.game_type::text AS game_type
            FROM statistic_games g
            ORDER BY game_type ASC
            """;

    private static final RowMapper<SystemTimingDto> SYSTEM_TIMING_MAPPER = (rs, rowNum) -> new SystemTimingDto(
            rs.getString("name"),
            rs.getLong("min_interval_ns"),
            rs.getLong("max_interval_ns"),
            rs.getDouble("median_mean_interval_ns"),
            rs.getDouble("p95_mean_interval_ns"),
            rs.getLong("game_count")
    );

    private static final RowMapper<TimeSeriesPointDto> TIME_SERIES_MAPPER = (rs, rowNum) -> new TimeSeriesPointDto(
            rs.getLong("game_id"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getDouble("mean_interval_ns")
    );

    private static final RowMapper<GameFrameSummaryDto> GAME_SUMMARY_MAPPER = (rs, rowNum) -> new GameFrameSummaryDto(
            rs.getLong("game_id"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getString("game_type"),
            rs.getLong("duration"),
            nullableDouble(rs, "frame_mean_interval_ns"),
            nullableLong(rs, "frame_max_interval_ns")
    );

    private static final RowMapper<GameTimingDetailDto> GAME_TIMING_MAPPER = (rs, rowNum) -> new GameTimingDetailDto(
            rs.getString("name"),
            rs.getLong("min_interval_ns"),
            rs.getLong("max_interval_ns"),
            rs.getDouble("mean_interval_ns")
    );

    private static Double nullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private static Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    public List<SystemTimingDto> findSystemTimings(PerformanceFilterDto filter) {
        return filtered(FIND_SYSTEM_TIMINGS, filter).query(SYSTEM_TIMING_MAPPER).list();
    }

    public List<TimeSeriesPointDto> findTimeSeries(String name, PerformanceFilterDto filter) {
        return filtered(FIND_TIME_SERIES, filter)
                .param("name", name)
                .query(TIME_SERIES_MAPPER)
                .list();
    }

    public PageDto<GameFrameSummaryDto> findRecentGames(PerformanceFilterDto filter, int page, int size) {
        List<GameFrameSummaryDto> content = filtered(FIND_RECENT_GAMES, filter)
                .param("frameName", frameName())
                .param("limit", size)
                .param("offset", (long) page * size)
                .query(GAME_SUMMARY_MAPPER)
                .list();
        long total = filtered(COUNT_RECENT_GAMES, filter).query(Long.class).single();
        return new PageDto<>(content, page, size, total);
    }

    public List<GameTimingDetailDto> findGameTimings(long gameId) {
        return jdbcClient.sql(FIND_GAME_TIMINGS)
                .param("gameId", gameId)
                .query(GAME_TIMING_MAPPER)
                .list();
    }

    public Optional<GameFrameSummaryDto> findGame(long gameId) {
        return jdbcClient.sql(FIND_GAME)
                .param("gameId", gameId)
                .param("frameName", frameName())
                .query(GAME_SUMMARY_MAPPER)
                .optional();
    }

    /** Distinct game types actually present, so the filter dropdown offers only real options. */
    public List<String> findGameTypes() {
        return jdbcClient.sql(FIND_GAME_TYPES).query(String.class).list();
    }

    private String frameName() {
        return GameResultBuilder.FRAME_STATISTIC_NAME;
    }

    private JdbcClient.StatementSpec filtered(String sql, PerformanceFilterDto filter) {
        return jdbcClient.sql(sql)
                .param("from", filter.from())
                .param("to", filter.to())
                .param("gameType", filter.gameType());
    }
}
