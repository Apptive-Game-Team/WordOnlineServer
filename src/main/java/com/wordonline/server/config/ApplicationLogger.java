package com.wordonline.server.config;

import com.wordonline.server.game.component.SessionManager;
import com.wordonline.server.matching.component.MatchingManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationLogger {
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationLogger {
    private final long LOGGING_INTERVAL = TimeUnit.MINUTES.toMillis(30); // 30 min

    private final MatchingManager matchingManager;
    private final SessionManager sessionManager;

    @Scheduled(fixedRate = LOGGING_INTERVAL)
    public void logApplicationHealth() {
        log.info("[Health Check] {} | Sessions {}", matchingManager.getHealthLog(), sessionManager.getHealthLog());
    }
}
