package com.wordonline.server.game.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.h2.Driver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

class PveScenarioRepositoryTest {

    private JdbcClient jdbcClient;
    private PveScenarioRepository pveScenarioRepository;

    @BeforeEach
    void setUp() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource(
                new Driver(),
                "jdbc:h2:mem:pve_scenario_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
        jdbcClient = JdbcClient.create(dataSource);
        jdbcClient.sql("""
                CREATE TABLE pve_scenario_installers (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    scenario_id BIGINT NOT NULL,
                    installer_id VARCHAR(50) NOT NULL,
                    prefab_type VARCHAR(50) NOT NULL,
                    master VARCHAR(20) NOT NULL,
                    position_x INT NOT NULL,
                    position_y INT NOT NULL,
                    position_z INT NOT NULL,
                    sort_order INT NOT NULL
                )
                """).update();
        jdbcClient.sql("""
                CREATE TABLE pve_scenario_objectives (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    scenario_id BIGINT NOT NULL,
                    installer_id VARCHAR(50) NOT NULL,
                    sort_order INT NOT NULL
                )
                """).update();
        jdbcClient.sql("""
                CREATE TABLE pve_scenario_events (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    scenario_id BIGINT NOT NULL,
                    event_id VARCHAR(50) NOT NULL,
                    trigger_type VARCHAR(50) NOT NULL,
                    trigger_value INT NOT NULL,
                    speaker_installer_id VARCHAR(50),
                    message_key VARCHAR(100) NOT NULL,
                    sort_order INT NOT NULL
                )
                """).update();
        jdbcClient.sql("""
                CREATE TABLE pve_scenario_event_lines (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    event_row_id BIGINT NOT NULL,
                    line_order INT NOT NULL,
                    line_text VARCHAR(255) NOT NULL
                )
                """).update();
        pveScenarioRepository = new PveScenarioRepository(jdbcClient);
    }

    @Test
    void returnsEmptyForUnknownScenarioId() {
        assertThat(pveScenarioRepository.findById(9999L)).isEmpty();
    }

    @Test
    void returnsScenarioWithInstalledObjects() {
        jdbcClient.sql("""
                INSERT INTO pve_scenario_installers
                    (scenario_id, installer_id, prefab_type, master, position_x, position_y, position_z, sort_order)
                VALUES (1, 'boss', 'FireSlime', 'RightPlayer', 1, 2, 3, 0)
                """).update();
        jdbcClient.sql("""
                INSERT INTO pve_scenario_objectives (scenario_id, installer_id, sort_order)
                VALUES (1, 'boss', 0)
                """).update();

        var scenario = pveScenarioRepository.findById(1L);

        assertThat(scenario).isPresent();
        assertThat(scenario.get().installers()).hasSize(1);
        assertThat(scenario.get().objectiveInstallerIds()).containsExactly("boss");
    }
}
