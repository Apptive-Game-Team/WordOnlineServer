package com.wordonline.server.session.service;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.ResultChecker;
import com.wordonline.server.game.service.UserService;
import com.wordonline.server.game.service.UserScenarioService;
import com.wordonline.server.session.dto.RoomInfoDto;
import com.wordonline.server.session.dto.SessionDto;
import com.wordonline.server.session.util.GameLoopFactory;
import com.wordonline.server.session.util.SessionObjectFactory;
import com.wordonline.server.statistic.service.StatisticService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private static final Map<String, SessionObject> sessions = new ConcurrentHashMap<>();
    private static final Map<String, String> sessionIdsByAttemptId = new ConcurrentHashMap<>();

    private final SubmissionPublisher<Integer> onSessionNumChange = new SubmissionPublisher<>();

    private final SessionObjectFactory sessionObjectFactory;
    private final GameLoopFactory gameLoopFactory;
    private final StatisticService statisticService;
    private final UserService userService;
    private final UserScenarioService userScenarioService;

    public SessionService(SessionObjectFactory sessionObjectFactory,
                          GameLoopFactory gameLoopFactory,
                          StatisticService statisticService,
                          UserService userService,
                          UserScenarioService userScenarioService) {
        this.sessionObjectFactory = sessionObjectFactory;
        this.gameLoopFactory = gameLoopFactory;
        this.statisticService = statisticService;
        this.userService = userService;
        this.userScenarioService = userScenarioService;
    }

    public void subscribeSessionNumChange(Flow.Subscriber<Integer> subscriber) {
        onSessionNumChange.subscribe(subscriber);
    }

    public void createSession(SessionDto sessionDto) {
        createSessionObject(sessionDto);
    }

    public synchronized SessionCreationResult createSession(String attemptId, SessionDto sessionDto) {
        if (attemptId == null || attemptId.isBlank()) {
            throw new IllegalArgumentException("attemptId must not be blank");
        }

        String existingSessionId = sessionIdsByAttemptId.get(attemptId);
        if (existingSessionId != null) {
            if (!existingSessionId.equals(sessionDto.sessionId())) {
                throw new IllegalArgumentException("attemptId is already associated with another session");
            }
            return new SessionCreationResult(attemptId, existingSessionId, isSessionActive(existingSessionId));
        }

        SessionObject existingSession = sessions.get(sessionDto.sessionId());
        if (existingSession != null) {
            throw new IllegalArgumentException("sessionId already exists");
        }

        createSessionObject(sessionDto);
        sessionIdsByAttemptId.put(attemptId, sessionDto.sessionId());
        return new SessionCreationResult(attemptId, sessionDto.sessionId(), isSessionActive(sessionDto.sessionId()));
    }

    private void createSessionObject(SessionDto sessionDto) {
        SessionObject sessionObject = sessionObjectFactory.createSessionObject(sessionDto);
        GameLoop loop = gameLoopFactory.create(sessionObject.getSessionType());

        sessionObject.setGameLoop(loop);
        loop.init(sessionObject, () -> onLoopTerminated(sessionObject));

        if (!sessionObject.getSessionId().contains("debug")) {
            statisticService.createBuilder(loop.getGameContext());
        }

        sessions.put(sessionObject.getSessionId(), sessionObject);
        Thread thread = new Thread(loop);
        try {
            thread.start();
        } catch (RuntimeException | Error exception) {
            sessions.remove(sessionObject.getSessionId(), sessionObject);
            throw exception;
        }
        submitSessionNumChange();
        log.info("[Session] Session created; sessionId: {}", sessionObject.getSessionId());
    }

    public boolean isSessionActive(String sessionId) {
        SessionObject sessionObject = sessions.get(sessionId);
        return sessionObject != null
                && sessionObject.getGameLoop() != null
                && sessionObject.getGameLoop().is_running();
    }

    private void onLoopTerminated(SessionObject sessionObject) {
        sessions.remove(sessionObject.getSessionId());
        sessionObject.getPingChecker().close();
        submitSessionNumChange();

        GameContext gameContext = sessionObject.getGameContext();
        ResultChecker resultChecker = gameContext.getResultChecker();
        Master loser = resultChecker.getLoser();

        try {
            statisticService.saveGameResult(gameContext, loser, sessionObject.getSessionType());
        } catch (Exception e) {
            log.warn("[Session] Failed to save game statistics; sessionId: {}", sessionObject.getSessionId(), e);
        }

        if (loser == null) {
            log.info("[Session] Session ended with no winner; sessionId: {}", sessionObject.getSessionId());
            return;
        }

        long winnerId = resultChecker.getWinnerId();

        if (!sessionObject.getSessionId().contains("debug")) {
            if (sessionObject.getSessionType() == SessionType.PVE && loser == Master.RightPlayer) {
                userScenarioService.markFinished(sessionObject.getLeftUserId(), sessionObject.getScenarioId());
            }
            if (winnerId >= 0) {
                userService.incrementTotalWins(winnerId);
            }
        }

        log.info("[Session] Session removed; sessionId: {}", sessionObject.getSessionId());
    }

    public void submitSessionNumChange() {
        onSessionNumChange.submit(sessions.size());
    }

    public SessionObject getSessionObject(String sessionId) {
        return sessions.get(sessionId);
    }

    public long getActiveSessions() {
        return sessions.values().stream().filter(s -> s.getGameLoop().is_running()).count();
    }

    public String getHealthLog() {
        long activeSessions = getActiveSessions();
        String joined = String.join("\n", sessions.values().stream().map(Object::toString).toList());
        return "Sessions: num :" + activeSessions + " | " + joined;
    }

    public Optional<SessionObject> findByUserId(long userId) {
        return sessions.values().stream()
                .filter(s -> s.getLeftUserId() == userId || s.getRightUserId() == userId)
                .findFirst();
    }

    public void clearSessions() {
        sessions.clear();
        sessionIdsByAttemptId.clear();
    }

    public List<RoomInfoDto> getAllActiveSessionsInfo(String baseUrl) {
        return sessions.values().stream()
                .filter(s -> s.getGameLoop() != null && s.getGameLoop().is_running())
                .map(s -> new RoomInfoDto(s.getSessionId(), Long.valueOf(s.getLeftUserId()), Long.valueOf(s.getRightUserId()), baseUrl))
                .toList();
    }
}
