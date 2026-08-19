package com.wordonline.server.bot.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * The application registers its properties records with {@code @ConfigurationPropertiesScan},
 * and {@link BotGameScheduler.SweepProperties} is a nested record. This pins that the scan finds
 * it and binds the key, which nothing else in the suite would catch until runtime.
 */
@SpringJUnitConfig(classes = SweepPropertiesScanTest.ScanConfig.class)
@TestPropertySource(properties = "bot.auto-match.max-sessions-per-sweep=7")
class SweepPropertiesScanTest {

    @Autowired
    private BotGameScheduler.SweepProperties sweepProperties;

    @Test
    void theScanFindsTheNestedRecordAndBindsTheKey() {
        assertThat(sweepProperties.maxSessionsPerSweep()).isEqualTo(7);
    }

    @Configuration
    @ConfigurationPropertiesScan("com.wordonline.server.bot")
    static class ScanConfig {
    }
}
