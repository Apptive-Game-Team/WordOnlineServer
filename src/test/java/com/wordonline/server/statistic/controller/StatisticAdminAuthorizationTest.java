package com.wordonline.server.statistic.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wordonline.server.auth.config.JwtAuthenticationFilter;
import com.wordonline.server.auth.config.JwtProvider;
import com.wordonline.server.auth.config.WebSecurityConfig;
import com.wordonline.server.service.LocalizationService;
import com.wordonline.server.statistic.dto.GameFrameSummaryDto;
import com.wordonline.server.statistic.dto.PageDto;
import com.wordonline.server.statistic.service.StatisticPerformanceService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The statistics endpoints must not be reachable by an ordinary logged-in player. Nothing else in
 * the codebase asserts this, and the failure mode is silent: the endpoints would simply answer.
 */
@WebMvcTest(controllers = {StatisticPerformanceController.class, StatisticPageController.class})
@Import({WebSecurityConfig.class, JwtAuthenticationFilter.class})
class StatisticAdminAuthorizationTest {

    private static final List<String> ADMIN_PATHS = List.of(
            "/api/admin/statistics/performance",
            "/api/admin/statistics/performance/series?name=Frame",
            "/api/admin/statistics/games",
            "/api/admin/statistics/games/1",
            "/admin/statistics",
            "/admin/statistics/games/1"
    );

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatisticPerformanceService statisticPerformanceService;

    @MockitoBean
    private JwtProvider jwtProvider;

    // Pulled in by GlobalExceptionHandler, which the web slice picks up as a @ControllerAdvice.
    @MockitoBean
    private LocalizationService localizationService;

    private void stubEmptyResults() {
        when(statisticPerformanceService.findSystemTimings(any())).thenReturn(List.of());
        when(statisticPerformanceService.findTimeSeries(any(), any())).thenReturn(List.of());
        when(statisticPerformanceService.findGameTypes()).thenReturn(List.of());
        when(statisticPerformanceService.findRecentGames(any(), anyInt(), anyInt()))
                .thenReturn(new PageDto<GameFrameSummaryDto>(List.of(), 0, 20, 0));
    }

    /**
     * Asserted as "rejected" rather than as one status code. This configuration answers an
     * anonymous caller with 403 and an authenticated but insufficiently privileged one with 401,
     * which is the reverse of the usual pairing; pinning either number here would be testing the
     * entry point rather than the access rule. What matters is that both are refused and that
     * neither is served statistics, which the service interaction check establishes.
     */
    @Test
    @WithAnonymousUser
    void unauthenticatedCallersAreRefusedAndServedNoStatistics() throws Exception {
        for (String path : ADMIN_PATHS) {
            mockMvc.perform(get(path)).andExpect(status().is4xxClientError());
        }
        verifyNoInteractions(statisticPerformanceService);
    }

    @Test
    @WithMockUser(authorities = "PLAYER")
    void anOrdinaryPlayerIsRefusedAndServedNoStatistics() throws Exception {
        for (String path : ADMIN_PATHS) {
            mockMvc.perform(get(path)).andExpect(status().is4xxClientError());
        }
        verifyNoInteractions(statisticPerformanceService);
    }

    @Test
    @WithMockUser(authorities = "WORDONLINE_ADMIN")
    void aWordonlineAdminIsAllowedThrough() throws Exception {
        stubEmptyResults();
        mockMvc.perform(get("/api/admin/statistics/performance")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/statistics")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "SUPER_ADMIN")
    void aSuperAdminIsAllowedThrough() throws Exception {
        stubEmptyResults();
        mockMvc.perform(get("/api/admin/statistics/performance")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/statistics")).andExpect(status().isOk());
    }
}
