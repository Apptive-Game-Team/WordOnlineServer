package com.wordonline.server.game.component;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.service.GameLoop;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SessionManager {
    private static Map<String, SessionObject> sessions = new java.util.concurrent.ConcurrentHashMap<>();

    public void createSession(SessionObject sessionObject) {
        GameLoop gameLoop = new GameLoop(sessionObject);
        sessionObject.setGameLoop(gameLoop);
        Thread thread = new Thread(gameLoop);
        thread.start();

        sessions.put(sessionObject.getLeftUserId(),sessionObject);
        sessions.put(sessionObject.getRightUserId(),sessionObject);
    }

    public void closeSession(SessionObject sessionObject) {
        sessions.remove(sessionObject.getRightUserId());
        sessions.remove(sessionObject.getLeftUserId());
    }

    public SessionObject getSessionObject(String userId) {
        return sessions.get(userId);
    }
}
