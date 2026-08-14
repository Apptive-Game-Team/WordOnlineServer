package com.wordonline.server.statistic.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wordonline.server.auth.config.JwtAuthenticationFilter;
import com.wordonline.server.auth.config.JwtProvider;
import com.wordonline.server.auth.config.WebSecurityConfig;
import com.wordonline.server.service.LocalizationService;
import com.wordonline.server.statistic.dto.GameFrameSummaryDto;
import com.wordonline.server.statistic.dto.GameTimingDetailDto;
import com.wordonline.server.statistic.dto.PageDto;
import com.wordonline.server.statistic.dto.SystemTimingDto;
import com.wordonline.server.statistic.dto.TimeSeriesPointDto;
import com.wordonline.server.statistic.service.StatisticPerformanceService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Renders the Thymeleaf templates for real. A template that throws at render time still compiles
 * and still passes a test that only checks the view name, so these assert on the produced HTML.
 */
@WebMvcTest(controllers = StatisticPageController.class)
@Import({WebSecurityConfig.class, JwtAuthenticationFilter.class})
@WithMockUser(authorities = "WORDONLINE_ADMIN")
class StatisticPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatisticPerformanceService statisticPerformanceService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private LocalizationService localizationService;

    private final SystemTimingDto frame =
            new SystemTimingDto("Frame", 5_000_000L, 90_000_000L, 30_000_000.0, 48_000_000.0, 5);
    private final SystemTimingDto physics =
            new SystemTimingDto("PhysicSystem", 1_000_000L, 4_000_000L, 2_000_000.0, 3_000_000.0, 5);

    private void stubPopulatedPage() {
        when(statisticPerformanceService.findSystemTimings(any())).thenReturn(List.of(frame, physics));
        when(statisticPerformanceService.findGameTypes()).thenReturn(List.of("PVP", "Practice"));
        when(statisticPerformanceService.findTimeSeries(any(), any())).thenReturn(List.of(
                new TimeSeriesPointDto(1L, LocalDateTime.of(2026, 8, 2, 10, 0), 20_000_000.0),
                new TimeSeriesPointDto(2L, LocalDateTime.of(2026, 8, 3, 10, 0), 40_000_000.0)));
        when(statisticPerformanceService.findRecentGames(any(), anyInt(), anyInt())).thenReturn(
                new PageDto<>(List.of(
                        new GameFrameSummaryDto(2L, LocalDateTime.of(2026, 8, 3, 10, 0), "PVP", 310L,
                                40_000_000.0, 95_000_000L),
                        // A game with no Frame row must render as a dash, not blow up on the null.
                        new GameFrameSummaryDto(3L, LocalDateTime.of(2026, 8, 4, 10, 0), "Practice", 60L,
                                null, null)),
                        0, 20, 45));
    }

    private String render(String path) throws Exception {
        return mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    void performancePageRendersFiltersSummaryChartsAndTable() throws Exception {
        stubPopulatedPage();

        mockMvc.perform(get("/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(view().name("statistic/performance"));
        String html = render("/admin/statistics");

        // Filters, including the game type options that keep bot load tests out of the numbers.
        assertThat(html).contains("name=\"gameType\"", ">PVP<", ">Practice<");
        // Summary row: 30ms median and 48ms p95 formatted from nanoseconds.
        assertThat(html).contains("30.00 ms", "48.00 ms");
        // Slowest first, so Frame precedes PhysicSystem in the table.
        assertThat(html.indexOf(">Frame<")).isLessThan(html.indexOf(">PhysicSystem<"));
        // Both canvases and the embedded chart payloads.
        assertThat(html).contains("id=\"timingChart\"", "id=\"seriesChart\"");
        assertThat(html).contains("\"medianMeanIntervalNs\":3.0E7");
        // Recent games table links to detail, and the missing Frame row degrades to a dash.
        assertThat(html).contains("/admin/statistics/games/2", "/admin/statistics/games/3");
        assertThat(html).contains("Page 1 of 3");
        // No unresolved Thymeleaf expressions survived into the output.
        assertThat(html).doesNotContain("th:text", "${");
    }

    @Test
    void performancePageRendersWithNoDataAtAll() throws Exception {
        when(statisticPerformanceService.findSystemTimings(any())).thenReturn(List.of());
        when(statisticPerformanceService.findGameTypes()).thenReturn(List.of());
        when(statisticPerformanceService.findRecentGames(any(), anyInt(), anyInt()))
                .thenReturn(new PageDto<>(List.of(), 0, 20, 0));

        String html = render("/admin/statistics");

        assertThat(html).contains("No timing statistics recorded in this range.");
        assertThat(html).contains("No games in this range.");
        assertThat(html).doesNotContain("${");
    }

    @Test
    void performancePageAcceptsTheFilterQueryStringAndPreselectsTheGameType() throws Exception {
        stubPopulatedPage();

        String html = render("/admin/statistics?from=2026-08-01T00:00:00&to=2026-08-08T00:00:00"
                + "&gameType=PVP&name=PhysicSystem&page=0&size=20");

        // The datetime-local inputs keep the submitted range rather than resetting on each apply.
        assertThat(html).contains("value=\"2026-08-01T00:00\"", "value=\"2026-08-08T00:00\"");
        // The requested name wins over the Frame default, both in the chart heading and the form.
        assertThat(html).contains("<span>PhysicSystem</span> over time");
        // Matched without the attribute separator: the template wraps those attributes onto two
        // lines and Thymeleaf preserves the break.
        assertThat(html).contains("selected=\"selected\">PVP</option>");
        assertThat(html).doesNotContain("${");
    }

    @Test
    void gameDetailPageRendersEveryMeasuredName() throws Exception {
        when(statisticPerformanceService.findGame(eq(2L))).thenReturn(Optional.of(
                new GameFrameSummaryDto(2L, LocalDateTime.of(2026, 8, 3, 10, 0), "PVP", 310L,
                        40_000_000.0, 95_000_000L)));
        when(statisticPerformanceService.findGameTimings(eq(2L))).thenReturn(List.of(
                new GameTimingDetailDto("Frame", 5_000_000L, 95_000_000L, 40_000_000.0),
                new GameTimingDetailDto("PhysicSystem", 1_000_000L, 4_000_000L, 2_000_000.0)));

        String html = render("/admin/statistics/games/2");

        assertThat(html).contains("Frame", "PhysicSystem", "40.00 ms", "310 s");
        assertThat(html).doesNotContain("${");
    }

    @Test
    void gameDetailPageRendersAMessageForAnUnknownGame() throws Exception {
        when(statisticPerformanceService.findGame(anyLong())).thenReturn(Optional.empty());

        String html = render("/admin/statistics/games/404");

        assertThat(html).contains("Game not found");
        assertThat(html).doesNotContain("${");
    }
}
