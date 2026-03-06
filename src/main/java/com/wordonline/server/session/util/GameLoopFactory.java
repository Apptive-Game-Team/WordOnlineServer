package com.wordonline.server.session.util;

import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.PveLoop;
import com.wordonline.server.game.service.WordOnlineLoop;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class GameLoopFactory {
    private final ObjectProvider<WordOnlineLoop> wordOnlineLoopProvider;
    private final ObjectProvider<PveLoop> pveLoopProvider;

    public GameLoopFactory(ObjectProvider<WordOnlineLoop> wordOnlineLoopProvider, ObjectProvider<PveLoop> pveLoopProvider) {
        this.wordOnlineLoopProvider = wordOnlineLoopProvider;
        this.pveLoopProvider = pveLoopProvider;
    }

    public GameLoop create(SessionType sessionType) {
        if (sessionType == SessionType.PVE) {
            return pveLoopProvider.getObject();
        }
        return wordOnlineLoopProvider.getObject();
    }
}
