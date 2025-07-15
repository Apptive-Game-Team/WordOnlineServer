package com.wordonline.server.matching.controller;

import com.wordonline.server.matching.component.MatchingManager;
import com.wordonline.server.matching.dto.SimpleMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class MatchingController {
    @Autowired
    private MatchingManager matchingManager;

    @Autowired
    private SimpMessagingTemplate template;

    // matching queue request
    @MessageMapping("/game/match/queue")
    public void queueMatching(long userId) {
        log.info("queued userId: {}", userId);
        String message;
        if (matchingManager.enqueue(userId)) {
            message = "Successfully Enqueued";
        } else {
            message = "Failed to enqueue user";
        }
        template.convertAndSend(
                String.format("/queue/match-status/%s", userId),
                new SimpleMessageDto(message)
        );
        matchingManager.tryMatchUsers();
    }
}
