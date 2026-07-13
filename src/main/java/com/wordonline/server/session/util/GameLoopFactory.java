package com.wordonline.server.session.util;

import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.InputRelayLoop;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class GameLoopFactory {
    private final ObjectProvider<InputRelayLoop> inputRelayLoopProvider;

    public GameLoopFactory(ObjectProvider<InputRelayLoop> inputRelayLoopProvider) {
        this.inputRelayLoopProvider = inputRelayLoopProvider;
    }

    public GameLoop create(SessionType sessionType) {
        return inputRelayLoopProvider.getObject();
    }
}
