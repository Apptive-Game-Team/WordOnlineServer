package com.wordonline.server.debug.service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.wordonline.server.debug.dto.DebugActionResponseDto;
import com.wordonline.server.debug.dto.DebugGameRequestDto;
import com.wordonline.server.debug.dto.DebugGameResponseDto;
import com.wordonline.server.debug.dto.DebugMagicInfoDto;
import com.wordonline.server.debug.dto.DebugPrefabInfoDto;
import com.wordonline.server.debug.dto.DebugSpawnPrefabRequestDto;
import com.wordonline.server.debug.dto.DebugSummonMagicRequestDto;
import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.repository.MagicRepository;
import com.wordonline.server.session.dto.SessionDto;
import com.wordonline.server.session.service.SessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebugService {

    private static final AtomicInteger sessionIdCounter = new AtomicInteger(1);
    private static final String DEBUG_SESSION_PREFIX = "debug-";
    private static final String DEBUG_PVE_SESSION_PREFIX = "debug-pve-";

    private final SessionService sessionService;
    private final DeckService deckService;
    private final DatabaseMagicParser magicParser;
    private final MagicRepository magicRepository;

    private SessionObject debugSession;

    public DebugGameResponseDto enterPracticeSession(DebugGameRequestDto debugGameRequestDto) {
        DebugGameResponseDto dto = new DebugGameResponseDto(createPveDebugSession(debugGameRequestDto.userId(), -1));
        log.info("Entering practice session userId: {}", debugGameRequestDto.userId());
        return dto;
    }

    public DebugGameResponseDto enterTestSession(DebugGameRequestDto debugGameRequestDto) {
        DebugGameResponseDto dto = new DebugGameResponseDto(getSessionId());
        log.info("Entering test session userId: {}, side: {}", debugGameRequestDto.userId(), debugGameRequestDto.side());
        long userId = debugGameRequestDto.userId();
        if (debugGameRequestDto.side() == Master.LeftPlayer) {
            log.info("left");
            debugSession.setLeftUser(
                    userId,
                    deckService.getSelectedCards(userId)
            );
        } else if (debugGameRequestDto.side() == Master.RightPlayer) {
            log.info("right");
            debugSession.setRightUser(
                    userId,
                    deckService.getSelectedCards(userId)
            );
        }
        return dto;
    }

    private String getSessionId() {
        if (isActive(debugSession)) {
            return debugSession.getSessionId();
        }

        return createDebugSession();
    }

    private String createDebugSession() {
        return createDebugSession(0, 0);
    }

    private String createDebugSession(long uid1, long uid2) {
        return createDebugSession(DEBUG_SESSION_PREFIX, uid1, uid2, SessionType.PVP, null);
    }

    private String createPveDebugSession(long uid1, long uid2) {
        return createDebugSession(DEBUG_PVE_SESSION_PREFIX, uid1, uid2, SessionType.Practice, null);
    }

    private String createDebugSession(String sessionPrefix, long uid1, long uid2, SessionType sessionType, Long scenarioId) {
        SessionDto sessionDto = new SessionDto(
                sessionPrefix + sessionIdCounter.getAndIncrement(),
                uid1,
                uid2,
                sessionType,
                scenarioId
        );
        sessionService.createSession(sessionDto);
        debugSession = sessionService.getSessionObject(sessionDto.sessionId());
        return sessionDto.sessionId();
    }

    private boolean isActive(SessionObject debugSession) {
        if (debugSession == null) {
            return false;
        }
        return debugSession.getGameLoop().is_running();
    }

    public DebugActionResponseDto summonMagic(DebugSummonMagicRequestDto requestDto) {
        SessionObject session = sessionService.getSessionObject(requestDto.sessionId());
        if (session == null || !session.getGameLoop().is_running()) {
            log.warn("summonMagic: session not found or not running: {}", requestDto.sessionId());
            return new DebugActionResponseDto(false, "Session not found or not running.");
        }

        Magic magic = resolveMagic(requestDto);
        if (magic == null) {
            log.warn("summonMagic: magic not found. magicId: {}, magicName: {}", requestDto.magicId(), requestDto.magicName());
            return new DebugActionResponseDto(false, "Magic not found.");
        }

        GameContext gameContext = session.getGameContext();
        magic.run(gameContext, requestDto.master(), requestDto.position());
        log.info("summonMagic: magicId {}, magicName {} summoned for {} in session {}", requestDto.magicId(), requestDto.magicName(), requestDto.master(), requestDto.sessionId());
        return new DebugActionResponseDto(true, "Magic summoned successfully.");
    }

    public DebugActionResponseDto spawnPrefab(DebugSpawnPrefabRequestDto requestDto) {
        SessionObject session = sessionService.getSessionObject(requestDto.sessionId());
        if (session == null || !session.getGameLoop().is_running()) {
            log.warn("spawnPrefab: session not found or not running: {}", requestDto.sessionId());
            return new DebugActionResponseDto(false, "Session not found or not running.");
        }

        PrefabType prefabType = resolvePrefabType(requestDto);
        if (prefabType == null) {
            log.warn("spawnPrefab: prefab not found. prefabId: {}, prefabType: {}", requestDto.prefabId(), requestDto.prefabType());
            return new DebugActionResponseDto(false, "Prefab not found.");
        }

        GameContext gameContext = session.getGameContext();
        new GameObject(requestDto.master(), prefabType, requestDto.position(), gameContext);
        log.info("spawnPrefab: prefabId {}, prefabType {} spawned for {} in session {}", requestDto.prefabId(), prefabType, requestDto.master(), requestDto.sessionId());
        return new DebugActionResponseDto(true, "Prefab spawned successfully.");
    }

    public List<DebugMagicInfoDto> getMagicList() {
        return magicRepository.getAllMagic().stream()
                .map(magicInfo -> new DebugMagicInfoDto(magicInfo.id(), magicInfo.name()))
                .toList();
    }

    public List<DebugPrefabInfoDto> getPrefabList() {
        return Arrays.stream(PrefabType.values())
                .map(prefabType -> new DebugPrefabInfoDto(prefabType.getBeanName(), prefabType.name()))
                .toList();
    }

    private Magic resolveMagic(DebugSummonMagicRequestDto requestDto) {
        Long magicId = requestDto.magicId();
        if (magicId != null) {
            if (magicId <= 0) {
                return null;
            }
            return magicParser.parseMagicForBot(magicId);
        }

        if (requestDto.magicName() == null || requestDto.magicName().isBlank()) {
            return null;
        }

        return magicParser.parseMagicForBot(requestDto.magicName());
    }

    private PrefabType resolvePrefabType(DebugSpawnPrefabRequestDto requestDto) {
        if (requestDto.prefabType() != null) {
            return requestDto.prefabType();
        }

        String prefabId = requestDto.prefabId();
        if (prefabId == null || prefabId.isBlank()) {
            return null;
        }

        return Arrays.stream(PrefabType.values())
                .filter(prefabType -> prefabType.getBeanName().equals(prefabId))
                .findFirst()
                .orElse(null);
    }
}
