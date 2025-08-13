package com.wordonline.server.config;

import com.sun.management.OperatingSystemMXBean;
import com.wordonline.server.game.component.SessionManager;
import com.wordonline.server.matching.component.MatchingManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;


@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationLogger {
    private final long LOGGING_INTERVAL = 1000 * 60 * 10; // 10 min [ms]

    private final MatchingManager matchingManager;
    private final SessionManager sessionManager;
    private final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    @Scheduled(fixedRate = LOGGING_INTERVAL)
    public void logApplicationHealth() {

        log.info("[Health Check] {} | {} | {} | {}",
                getCpuLog(), getMemoryLog(),
                matchingManager.getHealthLog(),
                sessionManager.getHealthLog());
    }

    private String getCpuLog() {
        double cpuLoad = operatingSystemMXBean.getCpuLoad() * 100;
        return String.format("CPU Usage: %.2f", cpuLoad);
    }

    private String getMemoryLog() {
        long totalPhysicalMemory = operatingSystemMXBean.getTotalMemorySize();
        long freePhysicalMemory = operatingSystemMXBean.getFreeMemorySize();
        long usedPhysicalMemory = totalPhysicalMemory - freePhysicalMemory;

        return String.format("Used Memory: %.2f GB / %.2f GB",
                usedPhysicalMemory / 1024.0 / 1024.0 / 1024.0,
                totalPhysicalMemory / 1024.0 / 1024.0 / 1024.0);
    }
}
