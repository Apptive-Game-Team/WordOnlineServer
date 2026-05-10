package com.wordonline.server.game.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.wordonline.server.game.domain.RunType;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.simulation.SimulationBatchRequestDto;
import com.wordonline.server.game.dto.simulation.SimulationBatchResponseDto;
import com.wordonline.server.session.dto.SessionDto;
import com.wordonline.server.session.service.SessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationService {

    private static final int DEFAULT_MATCH_COUNT = 1;
    private static final int MAX_MATCH_COUNT = 100;
    private static final long POLL_INTERVAL_MS = 200L;
    private static final Duration MATCH_TIMEOUT = Duration.ofMinutes(2);
    private final SessionService sessionService;

    public SimulationBatchResponseDto runFixedMatchupBatch(SimulationBatchRequestDto requestDto) {
        validate(requestDto);

        int requestedMatchCount = requestDto.matchCount() == null ? DEFAULT_MATCH_COUNT : requestDto.matchCount();
        UUID simulationBatchId = UUID.randomUUID();

        int leftWins = 0;
        int rightWins = 0;
        int draws = 0;

        for (int matchNumber = 1; matchNumber <= requestedMatchCount; matchNumber++) {
            SessionObject sessionObject = createSimulationSession(requestDto, simulationBatchId, matchNumber);
            waitForCompletion(sessionObject);

            Master loser = sessionObject.getGameContext().getResultChecker().getLoser();
            if (loser == Master.LeftPlayer) {
                rightWins++;
            } else if (loser == Master.RightPlayer) {
                leftWins++;
            } else {
                draws++;
            }
        }

        return new SimulationBatchResponseDto(
                simulationBatchId,
                requestDto.leftUserId(),
                requestDto.rightUserId(),
                requestedMatchCount,
                requestedMatchCount,
                leftWins,
                rightWins,
                draws,
                requestDto.parameterProfileId()
        );
    }

    private SessionObject createSimulationSession(SimulationBatchRequestDto requestDto,
                                                  UUID simulationBatchId,
                                                  int matchNumber) {
        String sessionId = "simulation-" + simulationBatchId + "-" + matchNumber;
        SessionDto sessionDto = new SessionDto(
                sessionId,
                requestDto.leftUserId(),
                requestDto.rightUserId(),
                SessionType.PVP,
                null,
                RunType.SIMULATION,
                requestDto.parameterProfileId(),
                simulationBatchId
        );

        sessionService.createSession(sessionDto);
        SessionObject sessionObject = sessionService.getSessionObject(sessionId);
        if (sessionObject == null) {
            throw new IllegalStateException("Simulation session was not created: " + sessionId);
        }

        if (sessionObject.getGameLoop() instanceof WordOnlineLoop wordOnlineLoop) {
            wordOnlineLoop.activateBotForUser(requestDto.leftUserId());
            wordOnlineLoop.activateBotForUser(requestDto.rightUserId());
        } else {
            throw new IllegalStateException("Simulation requires a PVP loop: " + sessionId);
        }

        log.info(
                "Started simulation session {} for batch {} (leftUserId={}, rightUserId={}, parameterProfileId={})",
                sessionId,
                simulationBatchId,
                requestDto.leftUserId(),
                requestDto.rightUserId(),
                requestDto.parameterProfileId()
        );
        return sessionObject;
    }

    private void waitForCompletion(SessionObject sessionObject) {
        long deadline = System.nanoTime() + MATCH_TIMEOUT.toNanos();

        while (sessionObject.getGameLoop().is_running()) {
            if (System.nanoTime() > deadline) {
                sessionObject.getGameLoop().close();
                throw new IllegalStateException("Simulation session timed out: " + sessionObject.getSessionId());
            }

            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Simulation wait interrupted for session: " + sessionObject.getSessionId());
            }
        }
    }

    private void validate(SimulationBatchRequestDto requestDto) {
        if (requestDto.leftUserId() <= 0 || requestDto.rightUserId() <= 0) {
            throw new IllegalArgumentException("Simulation user IDs must be positive.");
        }
        if (requestDto.leftUserId() == requestDto.rightUserId()) {
            throw new IllegalArgumentException("Left and right simulation users must be different.");
        }

        int requestedMatchCount = requestDto.matchCount() == null ? DEFAULT_MATCH_COUNT : requestDto.matchCount();
        if (requestedMatchCount <= 0 || requestedMatchCount > MAX_MATCH_COUNT) {
            throw new IllegalArgumentException("matchCount must be between 1 and " + MAX_MATCH_COUNT + ".");
        }
    }
}
