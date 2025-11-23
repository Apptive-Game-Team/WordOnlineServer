package com.wordonline.server.session.service;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.session.dto.SessionDto;
import com.wordonline.server.session.util.SessionObjectFactory;
import com.wordonline.server.statistic.service.StatisticService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SubmissionPublisher;

// this class is used to manage the sessions
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private static final Map<String, SessionObject> sessions = new ConcurrentHashMap<>();

    private final SessionObjectFactory sessionObjectFactory;
    private final ObjectProvider<GameLoop> gameLoopProvider;
    private final StatisticService statisticService;

    public void createSession(SessionDto sessionDto) {
        SessionObject sessionObject = sessionObjectFactory.createSessionObject(sessionDto);
        GameLoop gameLoop = gameLoopProvider.getObject();
        sessionObject.setGameLoop(gameLoop);
        gameLoop.init(sessionObject, () -> onLoopTerminated(sessionObject));

        if (!sessionObject.getSessionId().contains("debug")) {
            statisticService.createBuilder(gameLoop.getGameContext());
        }

        Thread thread = new Thread(gameLoop);
        thread.start();

        sessions.put(sessionObject.getSessionId(), sessionObject);
        log.info("[Session] Session created; sessionId: {}", sessionObject.getSessionId());
    }

    public boolean isSessionActive(String sessionId) {
        return sessions.get(sessionId)
                .getGameLoop().is_running();
    }

    private void onLoopTerminated(SessionObject s) {
        sessions.remove(s.getSessionId());
        statisticService.saveGameResult(s.getGameContext(), s.getGameContext().getResultChecker().getLoser(), s.getSessionType());
        log.info("[Session] Session removed; sessionId: {}", s.getSessionId());
    }

    public SessionObject getSessionObject(String sessionId) {
        return sessions.get(sessionId);
    }

    public long getActiveSessions() {
        return sessions.values().stream().filter(sessionObject -> sessionObject.getGameLoop().is_running()).count();
    }

    public String getHealthLog() {
        long numOfActiveSessions = getActiveSessions();
        return "Sessions: num :" + numOfActiveSessions + " | " + String.join("\n", sessions.values()
                .stream()
                .map(Object::toString).toList());
    }

    public Optional<SessionObject> findByUserId(long userId) {
        return sessions.values().stream()
                .filter(s -> s.getLeftUserId() == userId || s.getRightUserId() == userId)
                .findFirst();
    }

    public void clearSessions() {
        sessions.clear();
    }
}
