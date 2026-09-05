package com.wordonline.server.session.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "game.loop.execution")
public record GameLoopExecutionProperties(Mode mode, Integer actorThreads) {

    public enum Mode {
        ACTOR,
        DEDICATED_THREAD
    }

    public GameLoopExecutionProperties {
        if (mode == null) {
            mode = Mode.ACTOR;
        }
        if (actorThreads == null) {
            actorThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
        }
        if (actorThreads < 1) {
            throw new IllegalArgumentException("game.loop.execution.actor-threads must be at least 1");
        }
    }
}
