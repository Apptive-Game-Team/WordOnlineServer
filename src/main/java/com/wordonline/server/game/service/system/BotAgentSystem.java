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
        int currentFrame = frameCounter.incrementAndGet();
        if (currentFrame % BOT_TICK_INTERVAL != 0) {
            return;
        }

        if (gameContext.getGameLoop() == null) {
            log.warn("[BotSystem] GameLoop is null, skipping bot update");
            return;
        }

        var wordOnlineLoop = gameContext.getGameLoop();
        log.trace("[BotSystem] Triggering bot tick at frame {}", currentFrame);

        var leftBotAgent = wordOnlineLoop.getLeftBotAgent();
        if (leftBotAgent != null) {
            if (leftBotProcessing.compareAndSet(false, true)) {
                var leftFrameInfoDto = wordOnlineLoop.getFrameDataSystem().getLeftFrameInfoDto();
                log.debug("[BotSystem] Submitting Left Bot task");
                botExecutorService.submit(() -> {
                    try {
                        leftBotAgent.onTick(leftFrameInfoDto);
                    } catch (Exception e) {
                        log.error("[BotSystem] Left bot agent execution error", e);
                    } finally {
                        leftBotProcessing.set(false);
                    }
                });
            } else {
                log.trace("[BotSystem] Left bot is still processing, skipping this tick");
            }
        }

        var rightBotAgent = wordOnlineLoop.getRightBotAgent();
        if (rightBotAgent != null) {
            if (rightBotProcessing.compareAndSet(false, true)) {
                var rightFrameInfoDto = wordOnlineLoop.getFrameDataSystem().getRightFrameInfoDto();
                log.debug("[BotSystem] Submitting Right Bot task");
                botExecutorService.submit(() -> {
                    try {
                        rightBotAgent.onTick(rightFrameInfoDto);
                    } catch (Exception e) {
                        log.error("[BotSystem] Right bot agent execution error", e);
                    } finally {
                        rightBotProcessing.set(false);
                    }
                });
            } else {
                log.trace("[BotSystem] Right bot is still processing, skipping this tick");
            }
        }
    }
}