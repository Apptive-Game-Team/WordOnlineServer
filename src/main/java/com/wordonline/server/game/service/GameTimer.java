package com.wordonline.server.game.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;

@Scope("prototype")
@Component
public class GameTimer {

    private final long startTime;
    private final long endTime;
    private final long duration;
    private final long feverTimeDuration;

    public GameTimer(Parameters parameters) {
        duration = (long) parameters.getValue("game", "duration");
        feverTimeDuration = (long) parameters.getValue("game", "fever_duration");
        startTime = System.currentTimeMillis();
        endTime = startTime + duration;
    }

    public long getRemainingTimeSeconds() {
        return (endTime - System.currentTimeMillis()) / 1000;
    }

    public boolean isEnd() {
        return getRemainingTimeSeconds() <= 0;
    }

    public boolean isFeverTime() {
        return getRemainingTimeSeconds() < feverTimeDuration;
    }
}
