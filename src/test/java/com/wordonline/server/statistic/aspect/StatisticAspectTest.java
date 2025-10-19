package com.wordonline.server.statistic.aspect;

import com.wordonline.server.game.dto.InputResponseDto;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.statistic.service.StatisticService;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticAspectTest {

    @InjectMocks
    private StatisticAspect statisticAspect;

    @Mock
    private StatisticService statisticService;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private GameContext gameContext;

    @Test
    @DisplayName("afterHandleInput 테스트 - response.isValid()가 false인 경우")
    void afterHandleInput_invalidResponse() {
        // given
        InputResponseDto response = new InputResponseDto(false, 0, 0, 0L);

        // when
        statisticAspect.afterHandleInput(joinPoint, response);

        // then
        verifyNoInteractions(statisticService);
    }

    @Test
    @DisplayName("afterHandleInput 테스트 - response.getMagicId()가 -1인 경우")
    void afterHandleInput_magicIdIsMinusOne() {
        // given
        InputResponseDto response = new InputResponseDto(true, 0, 0, -1L);

        // when
        statisticAspect.afterHandleInput(joinPoint, response);

        // then
        verifyNoInteractions(statisticService);
    }

    @Test
    @DisplayName("afterHandleInput 테스트 - 유효한 응답인 경우")
    void afterHandleInput_validResponse() {
        // given
        InputResponseDto response = new InputResponseDto(true, 100, 1, 1L);
        Object[] args = {gameContext, 1L};
        when(joinPoint.getArgs()).thenReturn(args);

        // when
        statisticAspect.afterHandleInput(joinPoint, response);

        // then
        verify(statisticService).saveMagic(gameContext, 1L, 1L);
    }
}
