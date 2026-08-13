package com.wordonline.server.statistic.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.statistic.service.StatisticService;
import com.wordonline.server.statistic.util.PjpUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Records the interval between the start of two consecutive frames.
 *
 * <p>{@link UpdateTimeAspect} measures how long each {@code GameSystem} takes to run, which is CPU
 * work and does not move when scheduling degrades: a frame that starts late still reports the same
 * per-system durations. The interval measured here is the axis that does move, so a loop running
 * below its target frame rate becomes visible.
 *
 * <p>{@code FrameDataSystem.earlyUpdate} is the first statement of {@code WordOnlineLoop.update()},
 * so it runs exactly once per frame. It is used as the join point instead of the loop's own
 * {@code update()} because {@code GameLoop.update()} is package-private and Spring AOP cannot
 * intercept non-public methods. Keeping the measurement in an aspect also keeps the {@code game}
 * package free of any dependency on {@code statistic}.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FrameIntervalAspect {

    private final StatisticService statisticService;

    @Before("execution(* com.wordonline.server.game.service.system.FrameDataSystem.earlyUpdate(..))")
    public void recordFrameStart(JoinPoint joinPoint) {
        long now = System.nanoTime();

        GameContext gameContext = PjpUtils.findArg(joinPoint.getArgs(), GameContext.class);

        log.trace("[FrameIntervalAspect] Method: {} Frame: {}",
                joinPoint.getSignature().toShortString(),
                gameContext.getFrameNum());

        statisticService.saveFrameStart(gameContext, now);
    }
}
