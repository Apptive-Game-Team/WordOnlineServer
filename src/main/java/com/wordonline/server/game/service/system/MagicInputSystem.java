package com.wordonline.server.game.service.system;

import com.wordonline.server.game.dto.input.InputRequestDto;
import com.wordonline.server.game.dto.input.InputResponseDto;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.MagicInputHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Executes magic inputs that were buffered by InputBufferSystem for the current frame.
 * Runs as the first step in the game loop update, before any physics or component updates.
 */
@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class MagicInputSystem implements GameSystem {

    private final InputBufferSystem inputBufferSystem;
    private final MagicInputHandler magicInputHandler;
    private final SimpMessagingTemplate template;

    @Override
    public void update(GameContext gameContext) {
        int frameNum = gameContext.getFrameNum();
        String sessionId = gameContext.getSessionObject().getSessionId();

        // Discard inputs from past frames that were never consumed
        inputBufferSystem.discardOlderThan(frameNum);

        Map<Long, InputRequestDto> inputs = inputBufferSystem.consume(frameNum);

        for (Map.Entry<Long, InputRequestDto> entry : inputs.entrySet()) {
            long userId = entry.getKey();
            InputRequestDto input = entry.getValue();

            if (input == null) continue;

            InputResponseDto response = magicInputHandler.handleInput(
                    gameContext, userId, input
            );

            // Send validation response directly back to the player
            template.convertAndSend(
                    String.format("/game/%s/frameInfos/%s", sessionId, userId),
                    response
            );
            log.trace("[MagicInputSystem] frame={} userId={} valid={}", frameNum, userId, response.valid());
        }
    }
}
