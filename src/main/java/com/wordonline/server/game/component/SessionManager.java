package com.wordonline.server.game.component;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.MmrService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

// this class is used to manage the sessions
@Service
@RequiredArgsConstructor
public class SessionManager {

    private static Map<String, SessionObject> sessions = new java.util.concurrent.ConcurrentHashMap<>();
    private final MmrService mmrService;

    public void createSession(SessionObject sessionObject) {
        GameLoop gameLoop = new GameLoop(sessionObject, mmrService);
        sessionObject.setGameLoop(gameLoop);
        Thread thread = new Thread(gameLoop);
        thread.start();

        sessions.put(sessionObject.getSessionId(),sessionObject);
    }

    public void closeSession(SessionObject sessionObject) {
        sessionObject.getGameLoop().close();
        sessions.remove(sessionObject.getSessionId());
    }

    public SessionObject getSessionObject(String sessionId) {
        return sessions.get(sessionId);
    }
}
