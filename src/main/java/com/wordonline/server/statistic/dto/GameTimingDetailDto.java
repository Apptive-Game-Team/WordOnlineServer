package com.wordonline.server.statistic.dto;

/**
 * One {@code statistic_update_time} row, as shown on the per-game detail view.
 *
 * @param name           the measured name
 * @param minIntervalNs  smallest interval measured for that name during the game
 * @param maxIntervalNs  largest interval measured for that name during the game
 * @param meanIntervalNs mean interval measured for that name during the game
 */
public record GameTimingDetailDto(
        String name,
        long minIntervalNs,
        long maxIntervalNs,
        double meanIntervalNs
) {
}
