package com.wordonline.server.session.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wordonline.server.session.service.SessionService;
import com.wordonline.server.session.dto.SessionDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/server")
public class SessionServerController {

    private final SessionService sessionService;

    @PostMapping("/game-sessions")
    public ResponseEntity<Boolean> createGameSession(@RequestBody SessionDto sessionDto) {
        sessionService.createSession(sessionDto);
        return ResponseEntity.ok(true);
    }
}
