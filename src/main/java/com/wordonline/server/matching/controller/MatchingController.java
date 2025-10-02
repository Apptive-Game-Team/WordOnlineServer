package com.wordonline.server.matching.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.matching.component.MatchingManager;
import com.wordonline.server.matching.dto.QueueLengthResponseDto;
import com.wordonline.server.matching.dto.SimpleMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
        log.info("[Queue] User queued for matching; userId: {}", principalDetails.userId);
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

    @ResponseBody
    @GetMapping("/api/match/length")
    public ResponseEntity<QueueLengthResponseDto> getQueueLength() {
        return ResponseEntity.ok(
                new QueueLengthResponseDto(matchingManager.getQueueLength())
        );
    }
}
