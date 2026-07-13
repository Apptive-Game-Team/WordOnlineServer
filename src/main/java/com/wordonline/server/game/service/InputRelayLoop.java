package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.lockstep.BootstrapEventDto;
import com.wordonline.server.game.dto.lockstep.BootstrapEventType;
import com.wordonline.server.game.dto.lockstep.ClientReadyDto;
import com.wordonline.server.game.dto.lockstep.ConfirmedFrameDto;
import com.wordonline.server.game.dto.lockstep.FrameSubmissionDto;
import com.wordonline.server.game.dto.lockstep.LockstepAbortDto;
import com.wordonline.server.game.dto.lockstep.LockstepAbortReason;
import com.wordonline.server.game.dto.lockstep.LockstepSessionStartDto;
import com.wordonline.server.game.service.lockstep.FrameResolution;
import com.wordonline.server.game.service.lockstep.LockstepFrameBuffer;
import com.wordonline.server.game.service.lockstep.LockstepReadyBarrier;
import com.wordonline.server.game.service.lockstep.LockstepMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Scope("prototype")
@Service
public class InputRelayLoop extends GameLoop {
    private final int protocolVersion;
    private final String simulationVersion;
    private final String configVersion;
    private final Duration frameTimeout;
    private final Duration readyTimeout;
    private final int maximumFutureFrames;
    private final LockstepMetrics metrics;
    private volatile LockstepFrameBuffer frameBuffer;
    private volatile LockstepReadyBarrier readyBarrier;
    private volatile boolean started;
    private final AtomicBoolean aborted = new AtomicBoolean();

    public InputRelayLoop(MmrService mmrService,
                          UserService userService,
                          GameContext gameContext,
                          Parameters parameters,
                          @Value("${game.lockstep.protocol-version:1}") int protocolVersion,
                          @Value("${game.lockstep.simulation-version:}") String simulationVersion,
                          @Value("${game.lockstep.config-version:}") String configVersion,
                          @Value("${game.lockstep.ready-timeout-ms:10000}") long readyTimeoutMs,
                          @Value("${game.lockstep.frame-timeout-ms:1000}") long frameTimeoutMs,
                          @Value("${game.lockstep.maximum-future-frames:2}") int maximumFutureFrames,
                          LockstepMetrics metrics) {
        super(mmrService, userService, gameContext, parameters);
        this.protocolVersion = protocolVersion;
        this.simulationVersion = requireVersion("simulation-version", simulationVersion);
        this.configVersion = requireVersion("config-version", configVersion);
        this.readyTimeout = Duration.ofMillis(readyTimeoutMs);
        this.frameTimeout = Duration.ofMillis(frameTimeoutMs);
        this.maximumFutureFrames = maximumFutureFrames;
        this.metrics = metrics;
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
        readyBarrier = new LockstepReadyBarrier(
                protocolVersion, simulationVersion, configVersion, participants);
    }

    public void ready(long userId, ClientReadyDto ready) {
        try {
            readyBarrier.ready(userId, ready);
        } catch (IllegalArgumentException exception) {
            abort(0, LockstepAbortReason.VERSION_MISMATCH, Set.of(userId));
            throw exception;
        }
    }

    public void disconnected(long userId) {
        if (started && isParticipant(userId)) {
            abort(frameBuffer.currentFrame(), LockstepAbortReason.PARTICIPANT_DISCONNECTED, Set.of(userId));
        }
    }

    private void startSession() {
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
                sessionObject.getRightUserCardDeck().snapshot(),
                bootstrapEvents());
        sendToPlayersAndSpectators(start);
        started = true;
        metrics.sessionStarted();
        log.info("[Lockstep] session started; sessionId={}, protocol={}, simulation={}, config={}",
                sessionObject.getSessionId(), protocolVersion, simulationVersion, configVersion);
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
            if (!started) {
                Set<Long> missing = readyBarrier.awaitReady(readyTimeout);
                if (!is_running()) {
                    return;
                }
                if (!missing.isEmpty()) {
                    abort(0, LockstepAbortReason.READY_TIMEOUT, missing);
                    return;
                }
                startSession();
            }
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
            metrics.frameConfirmed();
            log.debug("[Lockstep] frame confirmed; sessionId={}, frame={}, inputs={}",
                    sessionObject.getSessionId(), resolution.frameNum(), resolution.inputs().size());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            abort(gameContext.getFrameNum(), LockstepAbortReason.RELAY_INTERRUPTED, Set.of());
        }
    }

    private void abort(int frameNum, LockstepAbortReason reason, Set<Long> participants) {
        if (!aborted.compareAndSet(false, true)) {
            return;
        }
        log.warn("[Lockstep] abort sessionId={}, frame={}, reason={}, participants={}",
                sessionObject.getSessionId(), frameNum, reason, participants);
        sendToPlayersAndSpectators(new LockstepAbortDto(frameNum, reason, participants));
        metrics.sessionAborted(reason);
        close();
    }

    private boolean isParticipant(long userId) {
        return userId == sessionObject.getLeftUserId() || userId == sessionObject.getRightUserId();
    }

    private List<BootstrapEventDto> bootstrapEvents() {
        List<BootstrapEventDto> events = new java.util.ArrayList<>();
        events.add(new BootstrapEventDto(0, BootstrapEventType.SPAWN_PLAYER,
                Master.LeftPlayer, GameConfig.LEFT_PLAYER_POSITION, null));
        if (sessionObject.getSessionType() != SessionType.PVE) {
            events.add(new BootstrapEventDto(1, BootstrapEventType.SPAWN_PLAYER,
                    Master.RightPlayer, GameConfig.RIGHT_PLAYER_POSITION, null));
        } else {
            long scenarioId = sessionObject.getScenarioId() == null ? 1L : sessionObject.getScenarioId();
            events.add(new BootstrapEventDto(1, BootstrapEventType.START_PVE_SCENARIO,
                    null, null, scenarioId));
        }
        return List.copyOf(events);
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
