package com.wordonline.server.game.service.system;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.wordonline.server.game.service.GameContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Scope("prototype")
public class BotAgentSystem implements GameSystem {

    private final ExecutorService botExecutorService;
    private final AtomicBoolean leftBotProcessing = new AtomicBoolean(false);
    private final AtomicBoolean rightBotProcessing = new AtomicBoolean(false);
    private final AtomicInteger frameCounter = new AtomicInteger(0);

    private static final int BOT_TICK_INTERVAL = 8;

    @Override
    public void update(GameContext gameContext) {
        int currentFrame = frameCounter.incrementAndGet();
        if (currentFrame % BOT_TICK_INTERVAL != 0) return;

        var gameLoop = gameContext.getGameLoop();
        if (gameLoop == null) return;

        var leftBotAgent = gameLoop.getLeftBotAgent();
        if (leftBotAgent != null && leftBotProcessing.compareAndSet(false, true)) {
            botExecutorService.submit(() -> {
                try {
                    leftBotAgent.onTick();
                } catch (Exception e) {
                    log.error("[BotSystem] Left bot error", e);
                } finally {
                    leftBotProcessing.set(false);
                }
            });
        }

        var rightBotAgent = gameLoop.getRightBotAgent();
        if (rightBotAgent != null && rightBotProcessing.compareAndSet(false, true)) {
            botExecutorService.submit(() -> {
                try {
                    rightBotAgent.onTick();
                } catch (Exception e) {
                    log.error("[BotSystem] Right bot error", e);
                } finally {
                    rightBotProcessing.set(false);
                }
            });
        }
    }
}
