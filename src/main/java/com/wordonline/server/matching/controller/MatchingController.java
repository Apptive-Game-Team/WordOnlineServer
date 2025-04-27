package com.wordonline.server.matching.controller;

import com.wordonline.server.matching.component.MatchingManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class MatchingController {
    @Autowired
    private MatchingManager matchingManager;

    // for test code
    @MessageMapping("/echo")
    @SendTo("/topic/echo")
    public String echo(String message) {
        log.info("Received message: " + message);
        return message;
    }
}
