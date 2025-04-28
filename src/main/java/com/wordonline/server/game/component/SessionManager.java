package com.wordonline.server.game.component;

import com.wordonline.server.game.domain.SessionObject;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SessionManager {
    private static Map<String, SessionObject> sessions;

    public void createSession(SessionObject sessionObject) {
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
