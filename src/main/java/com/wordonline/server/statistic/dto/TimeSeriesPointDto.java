package com.wordonline.server.statistic.dto;

import java.time.LocalDateTime;

/**
 * One game's mean interval for a single measured name, for plotting that name over time.
 *
 * @param gameId         the {@code statistic_games} row this point came from
 * @param createdAt      when that game was recorded
 * @param meanIntervalNs the game's mean interval for the requested name
 */
public record TimeSeriesPointDto(
        long gameId,
        LocalDateTime createdAt,
        double meanIntervalNs
) {
}
