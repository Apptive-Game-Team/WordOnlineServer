package com.wordonline.server.statistic.dto;

import java.time.LocalDateTime;

/**
 * Time range and game type restriction shared by every performance query.
 * <p>
 * A null component means "no restriction". The game type filter matters more than it looks:
 * {@code BotGameScheduler} writes synthetic {@code Practice} games into the same tables as real
 * matches, so leaving it unset mixes load-test runs into the numbers.
 *
 * @param from      inclusive lower bound on {@code statistic_games.created_at}, or null
 * @param to        exclusive upper bound on {@code statistic_games.created_at}, or null
 * @param gameType  {@code statistic_games.game_type} to keep, or null for all types
 */
public record PerformanceFilterDto(
        LocalDateTime from,
        LocalDateTime to,
        String gameType
) {
    public static PerformanceFilterDto all() {
        return new PerformanceFilterDto(null, null, null);
    }
}
