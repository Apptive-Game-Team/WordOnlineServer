package com.wordonline.server.game.component;

import com.wordonline.server.auth.service.UserService;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.MmrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// this class is used to manage the sessions
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionManager {

    private static Map<String, SessionObject> sessions = new ConcurrentHashMap<>();
    private final MmrService mmrService;
    private final UserService userService;
    private final Parameters parameters;

    public void createSession(SessionObject sessionObject) {
        GameLoop gameLoop = new GameLoop(sessionObject, mmrService, userService, parameters);
        sessionObject.setGameLoop(gameLoop);
        Thread thread = new Thread(gameLoop);
        thread.start();

        sessions.put(sessionObject.getSessionId(), sessionObject);
        log.info("[Session] Session created; sessionId: {}", sessionObject.getSessionId());
    }

    public void closeSession(SessionObject sessionObject) {
        sessionObject.getGameLoop().close();
        sessions.remove(sessionObject.getSessionId());
    }

    public SessionObject getSessionObject(String sessionId) {
        return sessions.get(sessionId);
    }

    public String getHealthLog() {
        return "Sessions: " + String.join("\n", sessions.values()
                .stream()
                .map(Object::toString).toList());
    }
}
