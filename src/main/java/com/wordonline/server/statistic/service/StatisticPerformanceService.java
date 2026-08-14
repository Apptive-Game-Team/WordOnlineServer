package com.wordonline.server.statistic.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.wordonline.server.statistic.dto.GameFrameSummaryDto;
import com.wordonline.server.statistic.dto.GameTimingDetailDto;
import com.wordonline.server.statistic.dto.PageDto;
import com.wordonline.server.statistic.dto.PerformanceFilterDto;
import com.wordonline.server.statistic.dto.SystemTimingDto;
import com.wordonline.server.statistic.dto.TimeSeriesPointDto;
import com.wordonline.server.statistic.repository.StatisticPerformanceRepository;

import lombok.RequiredArgsConstructor;

/**
 * Read-only access to the recorded timing statistics for the admin views.
 */
@Service
@RequiredArgsConstructor
public class StatisticPerformanceService {

    /** Page sizes are clamped so a hand-edited query string cannot ask for the whole table. */
    static final int MAX_PAGE_SIZE = 200;
    static final int DEFAULT_PAGE_SIZE = 20;

    private final StatisticPerformanceRepository repository;

    public List<SystemTimingDto> findSystemTimings(PerformanceFilterDto filter) {
        return repository.findSystemTimings(filter);
    }

    public List<TimeSeriesPointDto> findTimeSeries(String name, PerformanceFilterDto filter) {
        return repository.findTimeSeries(name, filter);
    }

    public PageDto<GameFrameSummaryDto> findRecentGames(PerformanceFilterDto filter, int page, int size) {
        return repository.findRecentGames(filter, Math.max(page, 0), clampSize(size));
    }

    public List<GameTimingDetailDto> findGameTimings(long gameId) {
        return repository.findGameTimings(gameId);
    }

    public Optional<GameFrameSummaryDto> findGame(long gameId) {
        return repository.findGame(gameId);
    }

    public List<String> findGameTypes() {
        return repository.findGameTypes();
    }

    private int clampSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
