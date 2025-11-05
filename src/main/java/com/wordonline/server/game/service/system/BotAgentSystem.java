package com.wordonline.server.game.service.system;

import java.util.concurrent.ExecutorService;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.service.GameContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotAgentSystem implements GameSystem {

    private final ExecutorService botExecutorService;

    @Override
    public void update(GameContext gameContext) {
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
                log.trace("Bot agent execution error: {}", e.getMessage());
            }
        });
    }
}
