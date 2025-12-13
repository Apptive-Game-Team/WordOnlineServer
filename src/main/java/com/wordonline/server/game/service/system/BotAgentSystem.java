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
    
    // Bot tick interval: every 8 frames at 20 FPS = 400ms between bot decisions
    private static final int BOT_TICK_INTERVAL = 8;

    @Override
    public void update(GameContext gameContext) {
        // Only process bot tick every BOT_TICK_INTERVAL frames
        if (frameCounter.incrementAndGet() % BOT_TICK_INTERVAL != 0) {
            return;
        }
        
        if (!(gameContext.getGameLoop() instanceof com.wordonline.server.game.service.WordOnlineLoop)) {
            return;
        }
        
        var wordOnlineLoop = (com.wordonline.server.game.service.WordOnlineLoop) gameContext.getGameLoop();
        
        // Process left bot if exists
        var leftBotAgent = wordOnlineLoop.getLeftBotAgent();
        if (leftBotAgent != null && leftBotProcessing.compareAndSet(false, true)) {
            var leftFrameInfoDto = wordOnlineLoop.getFrameDataSystem().getLeftFrameInfoDto();
            botExecutorService.submit(() -> {
                try {
                    leftBotAgent.onTick(leftFrameInfoDto);
                } catch (Exception e) {
                    log.debug("Left bot agent execution error", e);
                } finally {
                    leftBotProcessing.set(false);
                }
            });
        }
        
        // Process right bot if exists
        var rightBotAgent = wordOnlineLoop.getRightBotAgent();
        if (rightBotAgent != null && rightBotProcessing.compareAndSet(false, true)) {
            var rightFrameInfoDto = wordOnlineLoop.getFrameDataSystem().getRightFrameInfoDto();
            botExecutorService.submit(() -> {
                try {
                    rightBotAgent.onTick(rightFrameInfoDto);
                } catch (Exception e) {
                    log.debug("Right bot agent execution error", e);
                } finally {
                    rightBotProcessing.set(false);
                }
            });
        }
    }
}
