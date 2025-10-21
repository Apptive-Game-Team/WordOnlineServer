package com.wordonline.server.statistic.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.system.GameSystem;
import com.wordonline.server.statistic.service.StatisticService;
import com.wordonline.server.statistic.util.PjpUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class UpdateTimeAspect {

    private final StatisticService statisticService;

    @Around("execution(* com.wordonline.server.game.service.system.GameSystem.update(..))")
    public Object profileUpdate(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        Object result = pjp.proceed();
        long end = System.nanoTime();

        GameContext gameContext = PjpUtils.findArg(pjp.getArgs(), GameContext.class);

        log.trace("[UpdateTimeAspect] Method: {} Args: {} Elapsed: {} ns",
                pjp.getSignature().toShortString(),
                pjp.getArgs(),
                (end - start));

        statisticService.saveUpdateTime(
                gameContext,
                (Class<? extends GameSystem>) pjp.getTarget().getClass(),
                end - start
        );

        return result;
    }
}
