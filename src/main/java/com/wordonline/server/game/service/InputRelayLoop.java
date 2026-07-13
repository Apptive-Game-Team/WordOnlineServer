package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.lockstep.ConfirmedFrameDto;
import com.wordonline.server.game.dto.lockstep.FrameSubmissionDto;
import com.wordonline.server.game.dto.lockstep.LockstepAbortDto;
import com.wordonline.server.game.dto.lockstep.LockstepAbortReason;
import com.wordonline.server.game.dto.lockstep.LockstepSessionStartDto;
import com.wordonline.server.game.service.lockstep.FrameResolution;
import com.wordonline.server.game.service.lockstep.LockstepFrameBuffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@Scope("prototype")
@Service
public class InputRelayLoop extends GameLoop {
    private final int protocolVersion;
    private final String simulationVersion;
    private final String configVersion;
    private final Duration frameTimeout;
    private final int maximumFutureFrames;
    private volatile LockstepFrameBuffer frameBuffer;

    public InputRelayLoop(MmrService mmrService,
                          UserService userService,
                          GameContext gameContext,
                          Parameters parameters,
                          @Value("${game.lockstep.protocol-version:1}") int protocolVersion,
                          @Value("${game.lockstep.simulation-version:}") String simulationVersion,
                          @Value("${game.lockstep.config-version:}") String configVersion,
                          @Value("${game.lockstep.frame-timeout-ms:1000}") long frameTimeoutMs,
                          @Value("${game.lockstep.maximum-future-frames:2}") int maximumFutureFrames) {
        super(mmrService, userService, gameContext, parameters);
        this.protocolVersion = protocolVersion;
        this.simulationVersion = requireVersion("simulation-version", simulationVersion);
        this.configVersion = requireVersion("config-version", configVersion);
        this.frameTimeout = Duration.ofMillis(frameTimeoutMs);
        this.maximumFutureFrames = maximumFutureFrames;
    }

    @Override
    public void init(SessionObject sessionObject, Runnable onTerminated) {
        gameContext.initRelay(sessionObject);
        initializeRelayLoop(sessionObject, onTerminated);
        Set<Long> participants = new LinkedHashSet<>();
        if (sessionObject.getLeftUserId() >= 0) {
            participants.add(sessionObject.getLeftUserId());
        }
        if (sessionObject.getRightUserId() >= 0) {
            participants.add(sessionObject.getRightUserId());
        }
        frameBuffer = new LockstepFrameBuffer(protocolVersion, 1, maximumFutureFrames, participants);

        LockstepSessionStartDto start = new LockstepSessionStartDto(
                protocolVersion,
                simulationVersion,
                configVersion,
                sessionObject.getRandomSeed(),
                1,
                sessionObject.getSessionType(),
                sessionObject.getLeftUserId(),
                sessionObject.getRightUserId(),
                sessionObject.getLeftUserCardDeck().snapshot(),
                sessionObject.getRightUserCardDeck().snapshot());
        sendToPlayersAndSpectators(start);
    }

    public void submit(long userId, FrameSubmissionDto submission) {
        LockstepFrameBuffer buffer = frameBuffer;
        if (buffer == null) {
            throw new IllegalStateException("Lockstep session has not started");
        }
        buffer.submit(userId, submission);
    }

    @Override
    void update() {
        try {
            FrameResolution resolution = frameBuffer.awaitCurrentFrame(frameTimeout);
            if (!resolution.complete()) {
                abort(resolution.frameNum(), LockstepAbortReason.INPUT_TIMEOUT, resolution.missingParticipantIds());
                return;
            }
            if (!resolution.hashMatched()) {
                abort(resolution.frameNum(), LockstepAbortReason.PEER_HASH_MISMATCH, resolution.hashes().keySet());
                return;
            }
            sendToPlayersAndSpectators(new ConfirmedFrameDto(
                    protocolVersion,
                    resolution.frameNum(),
                    resolution.inputs(),
                    resolution.hashes(),
                    true));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            abort(gameContext.getFrameNum(), LockstepAbortReason.RELAY_INTERRUPTED, Set.of());
        }
    }

    private void abort(int frameNum, LockstepAbortReason reason, Set<Long> participants) {
        log.warn("[Lockstep] abort sessionId={}, frame={}, reason={}, participants={}",
                sessionObject.getSessionId(), frameNum, reason, participants);
        sendToPlayersAndSpectators(new LockstepAbortDto(frameNum, reason, participants));
        close();
    }

    private void sendToPlayersAndSpectators(Object payload) {
        sessionObject.sendFrameInfo(sessionObject.getLeftUserId(), payload);
        sessionObject.sendFrameInfo(sessionObject.getRightUserId(), payload);
        sessionObject.broadcastFrameInfo(payload);
    }

    private static String requireVersion(String name, String version) {
        if (version == null || version.isBlank()) {
            throw new IllegalStateException("game.lockstep." + name + " must be configured when lockstep is enabled");
        }
        return version;
    }
}
