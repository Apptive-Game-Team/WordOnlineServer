package com.wordonline.server.session.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Loop watchdog tuning. The threshold is how long a session's last completed frame may
 * age before the session counts as stuck; at 20 FPS a healthy loop refreshes it every
 * 50ms, so the 10s default is two hundred missed frames - far beyond any GC pause.
 */
@ConfigurationProperties(prefix = "watchdog")
public record WatchdogProperties(Duration stuckThreshold) {

    public WatchdogProperties {
        if (stuckThreshold == null) {
            stuckThreshold = Duration.ofSeconds(10);
        }
    }
}
