package com.wordonline.server.matching.controller;

import com.wordonline.server.matching.component.MatchingManager;
import com.wordonline.server.matching.dto.SimpleMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class MatchingController {
    @Autowired
    private MatchingManager matchingManager;

    @Autowired
    private SimpMessagingTemplate template;

    // for test echo
    @MessageMapping("/echo")
    @SendTo("/topic/echo")
    public String echo(String message) {
        log.info("Received message: " + message);
        return message;
    }

    // matching queue request
    @MessageMapping("/game/match/queue")
    public void queueMatching(String userId) {
        matchingManager.enqueue(userId);
        template.convertAndSend(
                String.format("/queue/match-status/%s", userId),
                new SimpleMessageDto("Successfully Enqueued")
        );
        matchingManager.tryMatchUsers();
    }
}
