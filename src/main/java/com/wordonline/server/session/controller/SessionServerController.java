package com.wordonline.server.session.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wordonline.server.server.entity.ServerState;
import com.wordonline.server.server.service.ServerStatusService;
import com.wordonline.server.session.dto.SessionLengthDto;
import com.wordonline.server.session.dto.SimpleBooleanDto;
import com.wordonline.server.session.service.SessionService;
import com.wordonline.server.session.dto.SessionDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/server")
public class SessionServerController {

    private final SessionService sessionService;
    private final ServerStatusService serverStatusService;

    @PostMapping("/game-sessions")
    public ResponseEntity<SimpleBooleanDto> createGameSession(@RequestBody SessionDto sessionDto) {

        if (!serverStatusService.getCurrentState().equals(ServerState.ACTIVE)) {
            return ResponseEntity.badRequest().body(new SimpleBooleanDto(false));
        }

        sessionService.createSession(sessionDto);
        return ResponseEntity.ok(new SimpleBooleanDto(true));
    }

    @GetMapping("/game-sessions/{sessionId}/active")
    public ResponseEntity<SimpleBooleanDto> isGameSessionActive(@PathVariable String sessionId) {
        return ResponseEntity.ok(
                new SimpleBooleanDto(sessionService.isSessionActive(sessionId)));
    }

    @GetMapping("/game-sessions/length")
    public ResponseEntity<SessionLengthDto> getSessionsLength() {
        return ResponseEntity.ok(
                new SessionLengthDto((int) sessionService.getActiveSessions()));
    }
}
