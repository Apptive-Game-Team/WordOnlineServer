package com.wordonline.server.game.service.system;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.service.GameContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotAgentSystem implements GameSystem {

    private final ExecutorService botExecutorService;
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    @Override
    public void update(GameContext gameContext) {
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
