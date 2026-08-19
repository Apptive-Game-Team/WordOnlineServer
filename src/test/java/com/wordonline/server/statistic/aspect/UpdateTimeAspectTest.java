package com.wordonline.server.statistic.aspect;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.system.GameSystem;
import com.wordonline.server.statistic.service.StatisticService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.longThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * The advice now reads the GameContext out of a fixed argument position instead of searching the
 * argument array by type, so these tests pin the assumption that makes that legal: every join point
 * the pointcut matches is {@code GameSystem.update(GameContext)}.
 */
class UpdateTimeAspectTest {

    @Test
    void recordsOneIntervalPerUpdateAgainstTheGameContextTheSystemWasCalledWith() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AspectTestConfig.class)) {

            StubGameSystem system = context.getBean(StubGameSystem.class);
            assertThat(AopUtils.isAopProxy(system)).isTrue();

            StatisticService statisticService = context.getBean(StatisticService.class);
            GameContext first = mock(GameContext.class);
            GameContext second = mock(GameContext.class);

            system.update(first);
            system.update(second);
            system.update(second);

            verify(statisticService, times(1))
                    .saveUpdateTime(same(first), eq(StubGameSystem.class), anyLong());
            verify(statisticService, times(2))
                    .saveUpdateTime(same(second), eq(StubGameSystem.class), anyLong());
        }
    }

    @Test
    void measuresTheTimeTheSystemItselfSpent() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AspectTestConfig.class)) {

            StatisticService statisticService = context.getBean(StatisticService.class);
            SleepingGameSystem system = context.getBean(SleepingGameSystem.class);
            GameContext gameContext = mock(GameContext.class);

            system.update(gameContext);

            verify(statisticService).saveUpdateTime(
                    same(gameContext),
                    eq(SleepingGameSystem.class),
                    longThat(elapsedNs -> elapsedNs >= SleepingGameSystem.SLEEP_NANOS)
            );
        }
    }

    static class StubGameSystem implements GameSystem {
        @Override
        public void update(GameContext gameContext) {
            // no-op: the advice, not the system body, is under test
        }
    }

    static class SleepingGameSystem implements GameSystem {
        static final long SLEEP_NANOS = 2_000_000L;

        @Override
        public void update(GameContext gameContext) {
            long deadline = System.nanoTime() + SLEEP_NANOS;
            while (System.nanoTime() < deadline) {
                Thread.onSpinWait();
            }
        }
    }

    /**
     * proxyTargetClass mirrors Spring Boot's AopAutoConfiguration default, which this application
     * does not override.
     */
    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class AspectTestConfig {

        @Bean
        StatisticService statisticService() {
            return mock(StatisticService.class);
        }

        @Bean
        UpdateTimeAspect updateTimeAspect(StatisticService statisticService) {
            return new UpdateTimeAspect(statisticService);
        }

        @Bean
        StubGameSystem stubGameSystem() {
            return new StubGameSystem();
        }

        @Bean
        SleepingGameSystem sleepingGameSystem() {
            return new SleepingGameSystem();
        }
    }
}
