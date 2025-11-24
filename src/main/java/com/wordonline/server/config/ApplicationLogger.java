package com.wordonline.server.config;

import com.sun.management.OperatingSystemMXBean;
import com.wordonline.server.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;


@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationLogger {
    private final long LOGGING_INTERVAL = 1000 * 60 * 1; // 1 min [ms]

    private final SessionService sessionService;
    private final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    @Scheduled(fixedRate = LOGGING_INTERVAL)
    public void logApplicationHealth() {

        log.info("[Health Check]\n{}\n{}\n{}",
                getCpuLog(), getMemoryLog(),
                sessionService.getHealthLog());
    }

    private String getCpuLog() {
        double cpuLoadRaw = operatingSystemMXBean.getCpuLoad();
        if (cpuLoadRaw < 0) {
            return "CPU Usage: N/A";
        }
        double cpuLoad = cpuLoadRaw * 100;
        return String.format("CPU Usage: %.2f", cpuLoad);
    }

    private String getMemoryLog() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return String.format("Used Memory: %.2f GB / %.2f GB",
                usedMemory / 1024.0 / 1024.0 / 1024.0,
                totalMemory / 1024.0 / 1024.0 / 1024.0);
    }
}
