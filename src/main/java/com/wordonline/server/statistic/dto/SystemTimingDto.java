package com.wordonline.server.statistic.dto;

/**
 * Aggregate timings for one measured name across every game in the filtered range.
 * <p>
 * The two percentiles describe the distribution of the per-game {@code mean_interval_ns} values.
 * They are deliberately not an average: each game's mean is itself an average over a different and
 * unrecorded number of frames, so averaging them again would weight every game equally regardless
 * of length. The median and p95 of the per-game means need no such weighting to be meaningful.
 *
 * @param name              the measured name, e.g. a {@code GameSystem} class name or {@code Frame}
 * @param minIntervalNs     smallest interval seen in any game in range
 * @param maxIntervalNs     largest interval seen in any game in range
 * @param medianMeanIntervalNs median of the per-game mean intervals
 * @param p95MeanIntervalNs 95th percentile of the per-game mean intervals
 * @param gameCount         number of games contributing a row for this name
 */
public record SystemTimingDto(
        String name,
        long minIntervalNs,
        long maxIntervalNs,
        double medianMeanIntervalNs,
        double p95MeanIntervalNs,
        long gameCount
) {
}
