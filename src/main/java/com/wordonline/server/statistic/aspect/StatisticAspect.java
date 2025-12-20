package com.wordonline.server.statistic.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.wordonline.server.game.dto.input.InputResponseDto;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.statistic.service.StatisticService;
import com.wordonline.server.statistic.util.PjpUtils;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class StatisticAspect {

    private final StatisticService statisticService;

    @AfterReturning(
            value = "execution(com.wordonline.server.game.dto.input.InputResponseDto com.wordonline.server.game.service.MagicInputHandler.handleInput(..))",
            returning = "response"
    )
    public void afterHandleInput(JoinPoint joinPoint, InputResponseDto response) {
        if (!response.valid() || response.magicId() == -1) {
            return;
        }

        GameContext gameContext = PjpUtils.findArg(joinPoint.getArgs(), GameContext.class);
        Long userId = PjpUtils.findArg(joinPoint.getArgs(), Long.class);

        statisticService.saveMagic(gameContext, userId, response.magicId());
    }


}
