package com.wordonline.server.statistic.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordonline.server.statistic.domain.GameResultBuilder;
import com.wordonline.server.statistic.dto.GameFrameSummaryDto;
import com.wordonline.server.statistic.dto.PageDto;
import com.wordonline.server.statistic.dto.PerformanceFilterDto;
import com.wordonline.server.statistic.dto.SystemTimingDto;
import com.wordonline.server.statistic.dto.TimeSeriesPointDto;
import com.wordonline.server.statistic.service.StatisticPerformanceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Server-rendered performance pages. Everything the charts draw is put in the model here, so the
 * page needs no second authenticated request from the browser to fill itself in.
 * <p>
 * Guarded by the same authority check as {@link StatisticPerformanceController}. Note that
 * {@code JwtAuthenticationFilter} only reads the {@code Authorization} header, so reaching these
 * pages requires a client that sets it; see the pull request for why a cookie fallback was not
 * added here.
 */
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WORDONLINE_ADMIN')")
@Controller
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticPageController {

    /** The loop targets 20 FPS, so a frame is over budget past 50ms. */
    static final long FRAME_BUDGET_NS = 50_000_000L;

    private final StatisticPerformanceService statisticPerformanceService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public String performance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String gameType,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        PerformanceFilterDto filter = new PerformanceFilterDto(from, to, blankToNull(gameType));
        List<SystemTimingDto> timings = statisticPerformanceService.findSystemTimings(filter);
        String selectedName = selectName(name, timings);
        List<TimeSeriesPointDto> series = selectedName == null
                ? List.of()
                : statisticPerformanceService.findTimeSeries(selectedName, filter);
        PageDto<GameFrameSummaryDto> games = statisticPerformanceService.findRecentGames(filter, page, size);

        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("gameType", filter.gameType());
        model.addAttribute("gameTypes", statisticPerformanceService.findGameTypes());
        model.addAttribute("timings", timings);
        model.addAttribute("selectedName", selectedName);
        model.addAttribute("series", series);
        model.addAttribute("games", games);
        model.addAttribute("frameTiming", frameTiming(timings));
        model.addAttribute("frameBudgetNs", FRAME_BUDGET_NS);
        model.addAttribute("timingsJson", toJson(timings));
        model.addAttribute("seriesJson", toJson(series));
        return "statistic/performance";
    }

    /**
     * Serialise for the embedded chart data. The result is written with {@code th:utext}, so any
     * literal {@code <} would let a measured name close the script element early; escaping it as a
     * unicode escape keeps the JSON valid and the element intact.
     */
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value).replace("<", "\\u003C");
        } catch (JsonProcessingException e) {
            // The charts are an enhancement; an unrenderable payload must not take out the page.
            log.warn("Could not serialise chart data", e);
            return "[]";
        }
    }

    @GetMapping("/games/{gameId}")
    public String gameDetail(@PathVariable long gameId, Model model) {
        GameFrameSummaryDto game = statisticPerformanceService.findGame(gameId).orElse(null);
        model.addAttribute("game", game);
        model.addAttribute("timings", game == null
                ? List.of()
                : statisticPerformanceService.findGameTimings(gameId));
        model.addAttribute("frameBudgetNs", FRAME_BUDGET_NS);
        return "statistic/game-detail";
    }

    /** Default the time series to Frame, which is the reason this page exists. */
    private String selectName(String requested, List<SystemTimingDto> timings) {
        if (requested != null && !requested.isBlank()) {
            return requested;
        }
        boolean hasFrame = timings.stream()
                .anyMatch(timing -> GameResultBuilder.FRAME_STATISTIC_NAME.equals(timing.name()));
        if (hasFrame) {
            return GameResultBuilder.FRAME_STATISTIC_NAME;
        }
        return timings.isEmpty() ? null : timings.getFirst().name();
    }

    private SystemTimingDto frameTiming(List<SystemTimingDto> timings) {
        return timings.stream()
                .filter(timing -> GameResultBuilder.FRAME_STATISTIC_NAME.equals(timing.name()))
                .findFirst()
                .orElse(null);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
