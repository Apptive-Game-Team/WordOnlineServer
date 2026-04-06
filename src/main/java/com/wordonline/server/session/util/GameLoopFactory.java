package com.wordonline.server.session.util;

import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.InputRelayLoop;
import com.wordonline.server.game.service.PveLockstepLoop;
import com.wordonline.server.game.service.PracticeLockstepLoop;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class GameLoopFactory {
    private final ObjectProvider<InputRelayLoop> pvpLoopProvider;
    private final ObjectProvider<PveLockstepLoop> pveLoopProvider;
    private final ObjectProvider<PracticeLockstepLoop> practiceLoopProvider;

    public GameLoopFactory(
            @Qualifier("inputRelayLoop") ObjectProvider<InputRelayLoop> pvpLoopProvider,
            ObjectProvider<PveLockstepLoop> pveLoopProvider,
            ObjectProvider<PracticeLockstepLoop> practiceLoopProvider) {
        this.pvpLoopProvider = pvpLoopProvider;
        this.pveLoopProvider = pveLoopProvider;
        this.practiceLoopProvider = practiceLoopProvider;
    }

    public GameLoop create(SessionType sessionType) {
        return switch (sessionType) {
            case PVP      -> pvpLoopProvider.getObject();
            case PVE      -> pveLoopProvider.getObject();
            case Practice -> practiceLoopProvider.getObject();
        };
    }
}
