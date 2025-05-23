package com.wordonline.server.game.controller;

import com.wordonline.server.game.component.SessionManager;
import com.wordonline.server.game.dto.InputRequestDto;
import com.wordonline.server.game.dto.InputResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@MessageMapping("/game/input")
public class InputController {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("{sessionId}/{userId}")
    public void handleInput(@DestinationVariable String sessionId, @DestinationVariable String userId, @Payload InputRequestDto inputRequestDto) {
        InputResponseDto responseDto = sessionManager.getSessionObject(sessionId).getGameLoop().inputHandler.handleInput(userId, inputRequestDto);
        template.convertAndSend(String.format("/game/%s/frameInfos/%s", sessionId, userId), responseDto);
    }
}
