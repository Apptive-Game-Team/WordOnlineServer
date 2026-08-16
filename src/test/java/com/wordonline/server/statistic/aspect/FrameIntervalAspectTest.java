package com.wordonline.server.statistic.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.system.FrameDataSystem;
import com.wordonline.server.game.service.system.SyncFrameDataSystem;
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
 * A pointcut that silently matches nothing is the main failure mode for this aspect, so these tests
 * assert against the expression declared on the production class rather than a copy of it.
 */
class FrameIntervalAspectTest {

    private static String declaredPointcut() throws NoSuchMethodException {
        return FrameIntervalAspect.class
                .getDeclaredMethod("recordFrameStart", JoinPoint.class)
                .getAnnotation(Before.class)
                .value();
    }

    private static AspectJExpressionPointcut pointcut() throws NoSuchMethodException {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(declaredPointcut());
        return pointcut;
    }

    @Test
    void matchesTheOncePerFrameCallOnTheSystemTheGameLoopActuallyHolds() throws Exception {
        // WordOnlineLoop declares its field as SyncFrameDataSystem and calls earlyUpdate as the
        // first statement of update(); PveLoop inherits that update() unchanged.
        assertThat(pointcut().matches(
                SyncFrameDataSystem.class.getMethod("earlyUpdate", GameContext.class),
                SyncFrameDataSystem.class
        )).isTrue();

        assertThat(pointcut().matches(
                FrameDataSystem.class.getMethod("earlyUpdate", GameContext.class),
                FrameDataSystem.class
        )).isTrue();
    }

    @Test
    void doesNotMatchTheEndOfFrameCallSoEachFrameIsCountedOnce() throws Exception {
        assertThat(pointcut().matches(
                SyncFrameDataSystem.class.getMethod("lateUpdate", GameContext.class),
                SyncFrameDataSystem.class
        )).isFalse();
    }

    @Test
    void springProxiesTheRealSystemAndFiresTheAdviceOnEveryFrame() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AspectTestConfig.class)) {

            // The real production bean must end up proxied, otherwise the advice can never run.
            Object real = context.getBean("syncFrameDataSystem");
            assertThat(AopUtils.isAopProxy(real)).isTrue();
            // And it must stay assignable to the concrete class, because WordOnlineLoop injects
            // SyncFrameDataSystem by type. A JDK interface proxy would fail that injection.
            assertThat(AopUtils.isCglibProxy(real)).isTrue();
            assertThat(real).isInstanceOf(SyncFrameDataSystem.class);

            StatisticService statisticService = context.getBean(StatisticService.class);
            GameContext gameContext = mock(GameContext.class);
            // earlyUpdate is stubbed out here; the aspect, not the frame payload, is under test.
            StubFrameDataSystem system = context.getBean(StubFrameDataSystem.class);

            system.earlyUpdate(gameContext);
            system.earlyUpdate(gameContext);

            verify(statisticService, times(2)).saveFrameStart(same(gameContext), anyLong());
        }
    }

    @Test
    void passesTheGameContextFromTheJoinPointSoIntervalsLandOnTheRightGame() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AspectTestConfig.class)) {

            StatisticService statisticService = context.getBean(StatisticService.class);
            GameContext first = mock(GameContext.class);
            GameContext second = mock(GameContext.class);
            StubFrameDataSystem system = context.getBean(StubFrameDataSystem.class);

            system.earlyUpdate(first);
            system.earlyUpdate(second);

            verify(statisticService).saveFrameStart(same(first), anyLong());
            verify(statisticService).saveFrameStart(same(second), anyLong());
        }
    }

    @Test
    void recordsAMonotonicTimestampTakenBeforeTheFrameRuns() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AspectTestConfig.class)) {

            StatisticService statisticService = context.getBean(StatisticService.class);
            StubFrameDataSystem system = context.getBean(StubFrameDataSystem.class);
            GameContext gameContext = mock(GameContext.class);

            long before = System.nanoTime();
            system.earlyUpdate(gameContext);
            long after = System.nanoTime();

            verify(statisticService).saveFrameStart(
                    eq(gameContext),
                    longThat(ns -> ns >= before && ns <= after)
            );
        }
    }

    /**
     * Subclass of the real system with the frame payload removed, so the advice can be exercised
     * without a fully initialised GameContext. It still goes through the same pointcut match.
     */
    static class StubFrameDataSystem extends SyncFrameDataSystem {
        @Override
        public void earlyUpdate(GameContext gameContext) {
            // no-op
        }
    }

    /**
     * proxyTargetClass mirrors Spring Boot's AopAutoConfiguration default
     * (spring.aop.proxy-target-class, matchIfMissing = true), which this application does not
     * override. Class-based proxies are required here: WordOnlineLoop injects the concrete
     * SyncFrameDataSystem, so an interface proxy would not satisfy the constructor.
     */
    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class AspectTestConfig {

        @Bean
        StatisticService statisticService() {
            return mock(StatisticService.class);
        }

        @Bean
        FrameIntervalAspect frameIntervalAspect(StatisticService statisticService) {
            return new FrameIntervalAspect(statisticService);
        }

        @Bean
        SyncFrameDataSystem syncFrameDataSystem() {
            return new SyncFrameDataSystem();
        }

        @Bean
        StubFrameDataSystem stubFrameDataSystem() {
            return new StubFrameDataSystem();
        }
    }
}
