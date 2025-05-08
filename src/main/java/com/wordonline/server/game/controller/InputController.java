package com.wordonline.server.game.controller;

import com.wordonline.server.game.dto.InputRequestDto;
import com.wordonline.server.game.dto.InputResponseDto;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;

@MessageMapping("/game/input")
public class InputController {

    @MessageMapping("{sessionId}/{userId}")
    public InputResponseDto handleInput(@DestinationVariable String sessionId, @DestinationVariable String userId, @Payload InputRequestDto inputRequestDto) {
        // TODO: Implement the logic to handle the input request
        return new InputResponseDto(true, 0);
    }
}
