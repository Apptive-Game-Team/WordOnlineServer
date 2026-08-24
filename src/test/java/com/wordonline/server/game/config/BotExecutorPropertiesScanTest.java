package com.wordonline.server.game.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * The application registers its properties records with {@code @ConfigurationPropertiesScan},
 * and {@link BotExecutorConfig.BotExecutorProperties} is a nested record. This pins that the scan
 * finds it and binds the keys, which nothing else in the suite would catch until runtime.
 */
@SpringJUnitConfig(classes = BotExecutorPropertiesScanTest.ScanConfig.class)
@TestPropertySource(properties = {
        "bot.executor.pool-size=3",
        "bot.executor.queue-capacity=16"
})
class BotExecutorPropertiesScanTest {

    @Autowired
    private BotExecutorConfig.BotExecutorProperties botExecutorProperties;

    @Test
    void theScanFindsTheNestedRecordAndBindsTheKeys() {
        assertThat(botExecutorProperties.poolSize()).isEqualTo(3);
        assertThat(botExecutorProperties.queueCapacity()).isEqualTo(16);
    }

    @Configuration
    @ConfigurationPropertiesScan("com.wordonline.server.game.config")
    static class ScanConfig {
    }
}
