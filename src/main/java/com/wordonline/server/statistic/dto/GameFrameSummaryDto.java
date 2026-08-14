package com.wordonline.server.statistic.dto;

import java.time.LocalDateTime;

/**
 * A row in the recent games list: the game plus its {@code Frame} timings.
 * <p>
 * The frame fields are nullable because the join is a left join: a game recorded before frame
 * interval collection existed, or one whose statistics were dropped, still lists with no frame data
 * rather than disappearing from the table.
 *
 * @param gameId              the {@code statistic_games} id
 * @param createdAt           when the game was recorded
 * @param gameType            the session type the game was played as
 * @param durationSeconds     how long the game lasted
 * @param frameMeanIntervalNs mean interval between frame starts, or null if not recorded
 * @param frameMaxIntervalNs  worst interval between frame starts, or null if not recorded
 */
public record GameFrameSummaryDto(
        long gameId,
        LocalDateTime createdAt,
        String gameType,
        long durationSeconds,
        Double frameMeanIntervalNs,
        Long frameMaxIntervalNs
) {
}
