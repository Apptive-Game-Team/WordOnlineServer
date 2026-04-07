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
import com.wordonline.server.session.dto.RoomListDto;
import com.wordonline.server.session.util.GameLoopFactory;
import com.wordonline.server.session.util.SessionObjectFactory;
import com.wordonline.server.statistic.service.StatisticService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Stream;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private static final long PENDING_TIMEOUT_MS = 30_000;

    private static final Map<String, SessionObject> sessions = new ConcurrentHashMap<>();
    private static final Map<String, PendingSession> pendingSessions = new ConcurrentHashMap<>();

    private record PendingSession(SessionObject sessionObject, Set<Long> readyUserIds, long createdAt) {}

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

    /** Account 서버가 호출. SessionObject만 생성하고 pending 상태로 대기. */
    public void createSession(SessionDto sessionDto) {
        SessionObject sessionObject = sessionObjectFactory.createSessionObject(sessionDto);
        pendingSessions.put(sessionObject.getSessionId(),
                new PendingSession(sessionObject, ConcurrentHashMap.newKeySet(), System.currentTimeMillis()));
        log.info("[Session] Pending session created; sessionId: {}", sessionObject.getSessionId());
    }

    /**
     * 클라이언트가 WebSocket 구독 완료 후 ready 신호를 보내면 호출.
     * JWT에서 추출한 userId가 해당 세션의 참여자인지 검증한 뒤,
     * 모든 human 플레이어가 ready되면 게임 루프를 시작한다.
     */
    public void onReady(String sessionId, long userId) {
        PendingSession pending = pendingSessions.get(sessionId);
        if (pending == null) {
            log.warn("[Session] Ready signal for unknown or already-started session: {}", sessionId);
            return;
        }

        SessionObject sessionObject = pending.sessionObject();
        if (sessionObject.getLeftUserId() != userId && sessionObject.getRightUserId() != userId) {
            throw new AuthorizationDeniedException("User not in session");
        }

        pending.readyUserIds().add(userId);
        log.info("[Session] Player ready; sessionId={} userId={}", sessionId, userId);

        boolean allReady = Stream.of(sessionObject.getLeftUserId(), sessionObject.getRightUserId())
                .filter(id -> id >= 0)
                .allMatch(pending.readyUserIds()::contains);

        if (allReady) {
            pendingSessions.remove(sessionId);
            startSession(sessionObject);
        }
    }

    private void startSession(SessionObject sessionObject) {
        GameLoop loop = gameLoopFactory.create(sessionObject.getSessionType());
        sessionObject.setGameLoop(loop);
        loop.init(sessionObject, () -> onLoopTerminated(sessionObject));

        if (!sessionObject.getSessionId().contains("debug")) {
            statisticService.createBuilder(loop.getGameContext());
        }

        Thread thread = new Thread(loop);
        thread.start();

        sessions.put(sessionObject.getSessionId(), sessionObject);
        log.info("[Session] Session started; sessionId: {}", sessionObject.getSessionId());
    }

    /** 30초마다 ready 신호 없이 방치된 pending 세션을 정리한다. */
    @Scheduled(fixedDelay = 30_000)
    public void cleanUpStalePendingSessions() {
        long now = System.currentTimeMillis();
        pendingSessions.entrySet().removeIf(entry -> {
            boolean stale = now - entry.getValue().createdAt() > PENDING_TIMEOUT_MS;
            if (stale) {
                log.warn("[Session] Pending session timed out; sessionId: {}", entry.getKey());
            }
            return stale;
        });
    }

    public boolean isSessionActive(String sessionId) {
        SessionObject session = sessions.get(sessionId);
        return session != null && session.getGameLoop().is_running();
    }

    private void onLoopTerminated(SessionObject sessionObject) {
        sessions.remove(sessionObject.getSessionId());
        submitSessionNumChange();

        GameContext gameContext = sessionObject.getGameContext();
        ResultChecker resultChecker = gameContext.getResultChecker();
        Master loser = resultChecker.getLoser();
        long winnerId = resultChecker.getWinnerId();

        statisticService.saveGameResult(gameContext, loser, sessionObject.getSessionType());

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
    }

    public List<RoomInfoDto> getAllActiveSessionsInfo(String baseUrl) {
        return sessions.values().stream()
                .filter(s -> s.getGameLoop() != null && s.getGameLoop().is_running())
                .map(s -> new RoomInfoDto(s.getSessionId(), Long.valueOf(s.getLeftUserId()), Long.valueOf(s.getRightUserId()), baseUrl))
                .toList();
    }
}
