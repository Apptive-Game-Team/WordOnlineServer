package com.wordonline.server.matching.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.auth.repository.UserRepository;
import com.wordonline.server.matching.component.MatchingManager;
import com.wordonline.server.matching.dto.QueueLengthResponseDto;
import com.wordonline.server.matching.dto.SimpleMessageDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingManager matchingManager;
    private final SimpMessagingTemplate template;
    private final UserRepository userRepository;

    // matching queue request
    @MessageMapping("/game/match/queue")
    public void queueMatching(@AuthenticationPrincipal PrincipalDetails principalDetails) {

        log.info("[Queue] User queued for matching; userId: {}", principalDetails.memberId);
        String message;
        if (matchingManager.enqueue(principalDetails.memberId)) {
            message = "Successfully Enqueued";
        } else {
            message = "Failed to enqueue user";
        }
        template.convertAndSend(
                String.format("/queue/match-status/%s", principalDetails.memberId),
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
