package com.wordonline.server.statistic.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.system.GameSystem;
import com.wordonline.server.statistic.service.StatisticService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class UpdateTimeAspect {

    private final StatisticService statisticService;

    /**
     * Runs once per {@code GameSystem} per frame, so everything here is on the frame budget.
     * {@code GameSystem} declares exactly one method, {@code update(GameContext)}, which is why the
     * context is read by index instead of searched for by type, and why the trace call is guarded:
     * Java evaluates the arguments of a disabled log statement anyway, and the signature string plus
     * the boxed elapsed time and the varargs array would be allocated on every frame for nothing.
     */
    @Around("execution(* com.wordonline.server.game.service.system.GameSystem.update(..))")
    public Object profileUpdate(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        Object result = pjp.proceed();
        long elapsedNs = System.nanoTime() - start;

        Object[] args = pjp.getArgs();
        GameContext gameContext = (GameContext) args[0];

        if (log.isTraceEnabled()) {
            log.trace("[UpdateTimeAspect] Method: {} Args: {} Elapsed: {} ns",
                    pjp.getSignature().toShortString(),
                    args,
                    elapsedNs);
        }

        statisticService.saveUpdateTime(
                gameContext,
                (Class<? extends GameSystem>) pjp.getTarget().getClass(),
                elapsedNs
        );

        return result;
    }
}
