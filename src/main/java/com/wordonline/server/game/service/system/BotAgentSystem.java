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
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private final AtomicInteger frameCounter = new AtomicInteger(0);
    
    // Bot tick interval: every 8 frames at 10 FPS = 800ms between bot decisions
    private static final int BOT_TICK_INTERVAL = 8;

    @Override
    public void update(GameContext gameContext) {
        // Only process bot tick every BOT_TICK_INTERVAL frames
        if (frameCounter.incrementAndGet() % BOT_TICK_INTERVAL != 0) {
            return;
        }
        
        // Only submit a new bot task if no bot operation is currently running
        if (isProcessing.compareAndSet(false, true)) {
            botExecutorService.submit(() -> {
                try {
                    gameContext
                            .getGameLoop()
                            .getBotAgent()
                            .onTick(
                                    gameContext.getGameLoop()
                                            .getFrameDataSystem()
                                            .getRightFrameInfoDto()
                            );
                } catch (Exception e) {
                    log.debug("Bot agent execution error: {}", e.getMessage());
                } finally {
                    isProcessing.set(false);
                }
            });
        }
    }
}
