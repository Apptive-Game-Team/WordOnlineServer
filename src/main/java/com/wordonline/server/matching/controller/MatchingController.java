package com.wordonline.server.matching.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.matching.component.MatchingManager;
import com.wordonline.server.matching.dto.SimpleMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public void queueMatching(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        log.trace("queued userId: {}", principalDetails.userId);
        String message;
        if (matchingManager.enqueue(principalDetails.userId)) {
            message = "Successfully Enqueued";
        } else {
            message = "Failed to enqueue user";
        }
        template.convertAndSend(
                String.format("/queue/match-status/%s", principalDetails.userId),
                new SimpleMessageDto(message)
        );
        matchingManager.tryMatchUsers();
    }
}
