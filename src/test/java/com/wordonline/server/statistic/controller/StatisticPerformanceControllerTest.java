package com.wordonline.server.statistic.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.wordonline.server.statistic.dto.GameFrameSummaryDto;
import com.wordonline.server.statistic.dto.GameTimingDetailDto;
import com.wordonline.server.statistic.dto.PageDto;
import com.wordonline.server.statistic.dto.PerformanceFilterDto;
import com.wordonline.server.statistic.dto.SystemTimingDto;
import com.wordonline.server.statistic.service.StatisticPerformanceService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatisticPerformanceControllerTest {

    private final StatisticPerformanceService service = mock(StatisticPerformanceService.class);
    private final StatisticPerformanceController controller = new StatisticPerformanceController(service);

    private final SystemTimingDto frame =
            new SystemTimingDto("Frame", 5_000_000L, 90_000_000L, 30_000_000.0, 48_000_000.0, 5);

    @Test
    void performancePassesTheRequestedFilterThroughAndReturnsTheTimings() {
        when(service.findSystemTimings(any())).thenReturn(List.of(frame));
        LocalDateTime from = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 8, 8, 0, 0);

        var response = controller.performance(from, to, "PVP");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsExactly(frame);

        ArgumentCaptor<PerformanceFilterDto> captor = ArgumentCaptor.forClass(PerformanceFilterDto.class);
        verify(service).findSystemTimings(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new PerformanceFilterDto(from, to, "PVP"));
    }

    @Test
    void anUnsetGameTypeDropdownMeansAllTypesRatherThanTheEmptyString() {
        when(service.findSystemTimings(any())).thenReturn(List.of());

        controller.performance(null, null, "   ");

        ArgumentCaptor<PerformanceFilterDto> captor = ArgumentCaptor.forClass(PerformanceFilterDto.class);
        verify(service).findSystemTimings(captor.capture());
        assertThat(captor.getValue().gameType()).isNull();
    }

    @Test
    void seriesRequestsTheNamedStatistic() {
        when(service.findTimeSeries(eq("Frame"), any())).thenReturn(List.of());

        var response = controller.series("Frame", null, null, null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(service).findTimeSeries(eq("Frame"), any());
    }

    @Test
    void gamesReturnsThePageTheServiceProduced() {
        GameFrameSummaryDto game = new GameFrameSummaryDto(
                7L, LocalDateTime.of(2026, 8, 3, 9, 30), "PVP", 300L, 30_000_000.0, 90_000_000L);
        PageDto<GameFrameSummaryDto> page = new PageDto<>(List.of(game), 1, 20, 45);
        when(service.findRecentGames(any(), anyInt(), anyInt())).thenReturn(page);

        var response = controller.games(null, null, null, 1, 20);

        assertThat(response.getBody()).isEqualTo(page);
        assertThat(response.getBody().totalPages()).isEqualTo(3);
        assertThat(response.getBody().hasNext()).isTrue();
        assertThat(response.getBody().hasPrevious()).isTrue();
        verify(service).findRecentGames(any(), eq(1), eq(20));
    }

    @Test
    void gameDetailReturnsTheTimingRowsForAKnownGame() {
        GameTimingDetailDto timing = new GameTimingDetailDto("Frame", 5_000_000L, 90_000_000L, 30_000_000.0);
        when(service.findGame(7L)).thenReturn(java.util.Optional.of(new GameFrameSummaryDto(
                7L, LocalDateTime.of(2026, 8, 3, 9, 30), "PVP", 300L, 30_000_000.0, 90_000_000L)));
        when(service.findGameTimings(7L)).thenReturn(List.of(timing));

        var response = controller.gameDetail(7L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsExactly(timing);
    }

    @Test
    void gameDetailIsNotFoundForAnUnknownGame() {
        when(service.findGame(anyLong())).thenReturn(java.util.Optional.empty());

        var response = controller.gameDetail(404L);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNull();
    }
}
