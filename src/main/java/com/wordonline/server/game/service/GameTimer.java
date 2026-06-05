package com.wordonline.server.game.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;

@Scope("prototype")
@Component
public class GameTimer {

    private final long startTime;
    private final long endTime;
    private final long duration;
    private final long feverTimeDuration;

    public GameTimer(Parameters parameters) {
        var gameParameters = parameters.object(GameObjectKey.GAME);
        duration = gameParameters.longValue(ParameterKey.DURATION);
        feverTimeDuration = gameParameters.longValue(ParameterKey.FEVER_DURATION);
        startTime = System.currentTimeMillis();
        endTime = startTime + duration;
    }

    public long getRemainingTimeMillis() {
        return endTime - System.currentTimeMillis();
    }

    public long getRemainingTimeSeconds() {
        return getRemainingTimeMillis() / 1000;
    }

    public boolean isEnd() {
        return getRemainingTimeMillis() <= 0;
    }

    public boolean isFeverTime() {
        return getRemainingTimeMillis() < feverTimeDuration;
    }
}
