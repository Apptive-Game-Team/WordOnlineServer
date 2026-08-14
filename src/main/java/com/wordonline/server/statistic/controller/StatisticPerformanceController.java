package com.wordonline.server.statistic.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordonline.server.statistic.dto.GameFrameSummaryDto;
import com.wordonline.server.statistic.dto.GameTimingDetailDto;
import com.wordonline.server.statistic.dto.PageDto;
import com.wordonline.server.statistic.dto.PerformanceFilterDto;
import com.wordonline.server.statistic.dto.SystemTimingDto;
import com.wordonline.server.statistic.dto.TimeSeriesPointDto;
import com.wordonline.server.statistic.service.StatisticPerformanceService;

import lombok.RequiredArgsConstructor;

/**
 * Timing statistics for admins. Authorisation comes from the class level {@code @PreAuthorize}
 * plus the {@code .anyRequest().authenticated()} tail of the filter chain; these paths are
 * deliberately absent from the chain's {@code permitAll()} list.
 */
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WORDONLINE_ADMIN')")
@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class StatisticPerformanceController {

    private final StatisticPerformanceService statisticPerformanceService;

    @GetMapping("/performance")
    public ResponseEntity<List<SystemTimingDto>> performance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String gameType) {
        return ResponseEntity.ok(statisticPerformanceService.findSystemTimings(filter(from, to, gameType)));
    }

    @GetMapping("/performance/series")
    public ResponseEntity<List<TimeSeriesPointDto>> series(
            @RequestParam String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String gameType) {
        return ResponseEntity.ok(statisticPerformanceService.findTimeSeries(name, filter(from, to, gameType)));
    }

    @GetMapping("/games")
    public ResponseEntity<PageDto<GameFrameSummaryDto>> games(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String gameType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(statisticPerformanceService.findRecentGames(filter(from, to, gameType), page, size));
    }

    @GetMapping("/games/{gameId}")
    public ResponseEntity<List<GameTimingDetailDto>> gameDetail(@PathVariable long gameId) {
        if (statisticPerformanceService.findGame(gameId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(statisticPerformanceService.findGameTimings(gameId));
    }

    private PerformanceFilterDto filter(LocalDateTime from, LocalDateTime to, String gameType) {
        return new PerformanceFilterDto(from, to, blankToNull(gameType));
    }

    // An unset dropdown submits an empty string; that must mean "all types", not "type ''".
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
