package com.wordonline.server.debug.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.debug.dto.DebugActionResponseDto;
import com.wordonline.server.debug.dto.DebugGameRequestDto;
import com.wordonline.server.debug.dto.DebugGameResponseDto;
import com.wordonline.server.debug.dto.DebugMagicInfoDto;
import com.wordonline.server.debug.dto.DebugPrefabInfoDto;
import com.wordonline.server.debug.dto.DebugSpawnPrefabRequestDto;
import com.wordonline.server.debug.dto.DebugSummonMagicRequestDto;
import com.wordonline.server.debug.service.DebugService;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.server.service.ServerUrlProvider;
import com.wordonline.server.session.dto.RoomListDto;
import com.wordonline.server.session.service.SessionService;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WORDONLINE_ADMIN')")
@Slf4j
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final DebugService debugService;
    private final SessionService sessionService;
    private final ServerUrlProvider serverUrlProvider;

    @PostMapping("/game/pve/{scenarioId}")
    public ResponseEntity<DebugGameResponseDto> createPveGame(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long scenarioId
    ) {
        log.info("createPveGame scenarioId {}", scenarioId);
        DebugGameRequestDto gameRequestDto = new DebugGameRequestDto(
                Master.LeftPlayer,
                principal.memberId,
                scenarioId
        );
        return ResponseEntity.ok(debugService.enterPracticeSession(gameRequestDto));
    }

    @PostMapping("/game/{side}") // side = left or right
    public ResponseEntity<DebugGameResponseDto> createGame(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable String side,
            @RequestParam(required = false) Long scenarioId
    ) {
        DebugGameRequestDto gameRequestDto;
        log.info("createGame side {}, scenarioId {}", side, scenarioId);

        if (side.compareToIgnoreCase("practice") == 0) {
            gameRequestDto = new DebugGameRequestDto(Master.LeftPlayer, principal.memberId, scenarioId);
            return ResponseEntity.ok(debugService.enterPracticeSession(gameRequestDto));
        }

        if (side.compareToIgnoreCase("left") == 0) {
            gameRequestDto = new DebugGameRequestDto(Master.LeftPlayer, principal.memberId, null);
        } else if (side.compareToIgnoreCase("right") == 0) {
            gameRequestDto = new DebugGameRequestDto(Master.RightPlayer, principal.memberId, null);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(debugService.enterTestSession(
                gameRequestDto
        ));
    }

    @PostMapping("/magic")
    public ResponseEntity<DebugActionResponseDto> summonMagic(
            @RequestBody DebugSummonMagicRequestDto requestDto
    ) {
        log.info("summonMagic sessionId: {}, magicId: {}, master: {}", requestDto.sessionId(), requestDto.magicId(), requestDto.master());
        DebugActionResponseDto response = debugService.summonMagic(requestDto);
        if (!response.success()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/magics")
    public ResponseEntity<List<DebugMagicInfoDto>> getMagics() {
        return ResponseEntity.ok(debugService.getMagicList());
    }

    @PostMapping("/prefab")
    public ResponseEntity<DebugActionResponseDto> spawnPrefab(
            @RequestBody DebugSpawnPrefabRequestDto requestDto
    ) {
        log.info("spawnPrefab sessionId: {}, prefabId: {}, master: {}", requestDto.sessionId(), requestDto.prefabId(), requestDto.master());
        DebugActionResponseDto response = debugService.spawnPrefab(requestDto);
        if (!response.success()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/prefabs")
    public ResponseEntity<List<DebugPrefabInfoDto>> getPrefabs() {
        return ResponseEntity.ok(debugService.getPrefabList());
    }

    @GetMapping("/game-sessions")
    public ResponseEntity<RoomListDto> getGameSessions() {
        return ResponseEntity.ok(
                new RoomListDto(sessionService.getAllActiveSessionsInfo(serverUrlProvider.getServerUrl())));
    }
}
