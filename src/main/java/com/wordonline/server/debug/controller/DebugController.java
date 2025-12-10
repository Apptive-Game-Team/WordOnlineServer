package com.wordonline.server.debug.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.debug.dto.DebugGameRequestDto;
import com.wordonline.server.debug.dto.DebugGameResponseDto;
import com.wordonline.server.debug.service.DebugService;
import com.wordonline.server.game.dto.Master;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WORDONLINE_ADMIN')")
@Slf4j
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final DebugService debugService;

    @PostMapping("/game/{side}") // side = left or right
    public ResponseEntity<DebugGameResponseDto> createGame(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable String side
    ) {
        DebugGameRequestDto gameRequestDto;
        log.info("createGame side {}", side);

        if (side.compareToIgnoreCase("practice") == 0) {
            gameRequestDto = new DebugGameRequestDto(Master.LeftPlayer, principal.memberId);
            return ResponseEntity.ok(debugService.enterPracticeSession(gameRequestDto));
        }

        if (side.compareToIgnoreCase("left") == 0) {
            gameRequestDto = new DebugGameRequestDto(Master.LeftPlayer, principal.memberId);
        } else if (side.compareToIgnoreCase("right") == 0) {
            gameRequestDto = new DebugGameRequestDto(Master.RightPlayer, principal.memberId);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(debugService.enterTestSession(
                gameRequestDto
        ));
    }
}
