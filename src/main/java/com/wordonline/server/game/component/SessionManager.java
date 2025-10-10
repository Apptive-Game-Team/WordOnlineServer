package com.wordonline.server.game.component;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.service.GameLoop;

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
public class SessionManager {

    private static final Map<String, SessionObject> sessions = new ConcurrentHashMap<>();

    public final SubmissionPublisher<Long> numOfSessionsFlow = new SubmissionPublisher<>();

    private final ObjectProvider<GameLoop> gameLoopProvider;

    public void createSession(SessionObject sessionObject) {
        GameLoop gameLoop = gameLoopProvider.getObject();
        gameLoop.init(sessionObject, () -> onLoopTerminated(sessionObject));
        sessionObject.setGameLoop(gameLoop);
        Thread thread = new Thread(gameLoop);
        thread.start();

        sessions.put(sessionObject.getSessionId(), sessionObject);
        log.info("[Session] Session created; sessionId: {}", sessionObject.getSessionId());
    }

    private void onLoopTerminated(SessionObject s) {
        sessions.remove(s.getSessionId());
        numOfSessionsFlow.submit(getActiveSessions());
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
