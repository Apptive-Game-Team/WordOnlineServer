package com.wordonline.server.session.util;

import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.InputRelayLoop;
import com.wordonline.server.game.service.PveLoop;
import com.wordonline.server.game.service.WordOnlineLoop;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GameLoopFactory {
    private final ObjectProvider<WordOnlineLoop> wordOnlineLoopProvider;
    private final ObjectProvider<PveLoop> pveLoopProvider;
    private final ObjectProvider<InputRelayLoop> inputRelayLoopProvider;
    private final boolean lockstepEnabled;

    public GameLoopFactory(@Qualifier("wordOnlineLoop") ObjectProvider<WordOnlineLoop> wordOnlineLoopProvider,
                           @Qualifier("pveLoop") ObjectProvider<PveLoop> pveLoopProvider,
                           ObjectProvider<InputRelayLoop> inputRelayLoopProvider,
                           @Value("${game.lockstep.enabled:false}") boolean lockstepEnabled) {
        this.wordOnlineLoopProvider = wordOnlineLoopProvider;
        this.pveLoopProvider = pveLoopProvider;
        this.inputRelayLoopProvider = inputRelayLoopProvider;
        this.lockstepEnabled = lockstepEnabled;
    }

    public GameLoop create(SessionType sessionType) {
        if (lockstepEnabled && sessionType == SessionType.PVP) {
            return inputRelayLoopProvider.getObject();
        }
        if (sessionType == SessionType.PVE) {
            return pveLoopProvider.getObject();
        }
        return wordOnlineLoopProvider.getObject();
    }
}
