package com.wordonline.server.session.service;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.ResultChecker;
import com.wordonline.server.game.service.UserService;
import com.wordonline.server.game.service.UserScenarioService;
import com.wordonline.server.lobby.client.LobbySessionClient;
import com.wordonline.server.session.dto.RoomInfoDto;
import com.wordonline.server.session.dto.SessionDto;
import com.wordonline.server.session.util.GameLoopFactory;
import com.wordonline.server.session.util.SessionObjectFactory;
import com.wordonline.server.statistic.service.StatisticService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private static final Duration SESSION_START_TIMEOUT = Duration.ofSeconds(2);
    private static final Map<String, SessionObject> sessions = new ConcurrentHashMap<>();
    private static final Map<String, String> sessionIdsByAttemptId = new ConcurrentHashMap<>();

    private final SubmissionPublisher<Integer> onSessionNumChange = new SubmissionPublisher<>();

    private final SessionObjectFactory sessionObjectFactory;
    private final GameLoopFactory gameLoopFactory;
    private final StatisticService statisticService;
    private final UserService userService;
    private final UserScenarioService userScenarioService;
    private final LobbySessionClient lobbySessionClient;

    public SessionService(SessionObjectFactory sessionObjectFactory,
                          GameLoopFactory gameLoopFactory,
                          StatisticService statisticService,
                          UserService userService,
                          UserScenarioService userScenarioService,
                          LobbySessionClient lobbySessionClient) {
        this.sessionObjectFactory = sessionObjectFactory;
        this.gameLoopFactory = gameLoopFactory;
        this.statisticService = statisticService;
        this.userService = userService;
        this.userScenarioService = userScenarioService;
        this.lobbySessionClient = lobbySessionClient;
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
        return new SessionCreationResult(attemptId, sessionDto.sessionId(), awaitSessionReady(sessionDto.sessionId()));
    }

    // isSessionActive() is only true once the loop thread is ticking, so the lobby would otherwise
    // race the thread start and be told a brand new session is not ready. Wait for the first tick
    // instead; a timeout leaves the session in place and reports not-ready so the lobby can retry
    // with the same attemptId.
    private boolean awaitSessionReady(String sessionId) {
        SessionObject sessionObject = sessions.get(sessionId);
        if (sessionObject == null || sessionObject.getGameLoop() == null) {
            return false;
        }

        boolean started = sessionObject.getGameLoop().awaitStart(SESSION_START_TIMEOUT);
        if (!started) {
            log.warn("[Session] Loop did not start within {}; sessionId: {}", SESSION_START_TIMEOUT, sessionId);
        }
        return started;
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

        notifyLobbySessionEnded(sessionObject);

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

    // The lobby's match ticket lives in Redis and stays MATCHED until it hears the session ended,
    // which would silently swallow the player's next queue attempt. The lobby reconciler is the
    // safety net, so a failed notification is tolerable here; a failed teardown is not.
    private void notifyLobbySessionEnded(SessionObject sessionObject) {
        String sessionId = sessionObject.getSessionId();
        if (sessionId.contains("debug")) {
            return;
        }

        try {
            lobbySessionClient.notifySessionEnded(sessionId);
        } catch (Exception e) {
            log.warn("[Session] Failed to notify the lobby of session end; sessionId: {}", sessionId, e);
        }
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

    // Ordered oldest first. Without an explicit sort the order is ConcurrentHashMap bucket order over
    // random UUID keys, so "the room near the top of the admin list" would mean nothing reproducible.
    public List<RoomInfoDto> getAllActiveSessionsInfo(String baseUrl) {
        return sessions.values().stream()
                .filter(s -> s.getGameLoop() != null && s.getGameLoop().is_running())
                .sorted(Comparator.comparing(SessionObject::getCreatedAt)
                        .thenComparing(SessionObject::getSessionId))
                .map(s -> new RoomInfoDto(
                        s.getSessionId(),
                        Long.valueOf(s.getLeftUserId()),
                        Long.valueOf(s.getRightUserId()),
                        baseUrl,
                        s.getCreatedAt()))
                .toList();
    }
}
