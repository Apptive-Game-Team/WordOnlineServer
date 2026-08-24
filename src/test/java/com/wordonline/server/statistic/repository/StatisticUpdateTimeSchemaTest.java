package com.wordonline.server.statistic.repository;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.wordonline.server.game.service.system.GameObjectStateInitialSystem;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The statistic_update_time definition in schema-h2.sql is now copied from the production
 * migration (V000, as widened by V066) rather than inferred from the INSERT in
 * {@link StatisticRepository}. These tests pin the two together so the shipped definition accepts
 * what the production code writes -- including frame intervals past the old INTEGER ceiling, which
 * only pass once V066 has been applied to the target database.
 */
class StatisticUpdateTimeSchemaTest {

    private static JdbcClient jdbcClient;

    @BeforeAll
    static void createSchema() throws Exception {
        DataSource dataSource = new SimpleDriverDataSource(
                new org.h2.Driver(),
                "jdbc:h2:mem:statisticUpdateTimeSchemaTest;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
        jdbcClient = JdbcClient.create(dataSource);
        List<String> ddl = statisticDdlFromSchemaFile();
        // Fail loudly rather than silently testing nothing if the definition goes missing.
        assertThat(ddl).hasSize(3);
        ddl.forEach(statement -> jdbcClient.sql(statement).update());
    }

    /**
     * Only the statistic DDL is applied, read out of the real schema-h2.sql so the definition under
     * test is the shipped one. The whole script cannot be executed here: it still contains
     * Postgres-only statements (multi-action ALTER TABLE, a column named "value") that H2 rejects,
     * which is a pre-existing gap unrelated to this table.
     */
    private static List<String> statisticDdlFromSchemaFile() throws Exception {
        String script = new ClassPathResource("schema-h2.sql")
                .getContentAsString(StandardCharsets.UTF_8);
        // Drop comment lines first: they may contain semicolons and would break naive splitting.
        String statements = script.lines()
                .filter(line -> !line.trim().startsWith("--"))
                .collect(Collectors.joining("\n"));
        return Arrays.stream(statements.split(";"))
                .map(String::trim)
                .filter(statement -> statement.contains("CREATE TABLE statistic_games")
                        || statement.contains("CREATE TABLE statistic_update_time")
                        || statement.contains("ON statistic_update_time"))
                .toList();
    }

    // Read the production statement rather than restating it, so the test cannot drift from it.
    private static String saveUpdateTimeSql() throws Exception {
        Field field = StatisticRepository.class.getDeclaredField("SAVE_UPDATE_TIME");
        field.setAccessible(true);
        return (String) field.get(null);
    }

    private long readLong(long gameId, String column) {
        return jdbcClient.sql("SELECT " + column + " FROM statistic_update_time WHERE statistic_game_id = ?")
                .param(gameId)
                .query(Long.class)
                .single();
    }

    private String readName(long gameId) {
        return jdbcClient.sql("SELECT name FROM statistic_update_time WHERE statistic_game_id = ?")
                .param(gameId)
                .query(String.class)
                .single();
    }

    private long insertGame() {
        jdbcClient.sql("""
                INSERT INTO statistic_games(
                    win_user_id,
                    loss_user_id,
                    duration,
                    game_type,
                    server_version,
                    event_schema_version
                )
                VALUES(1, 2, 300, 'PVP', 'test', 1);
                """).update();
        return jdbcClient.sql("SELECT MAX(id) FROM statistic_games").query(Long.class).single();
    }

    @Test
    void theInferredTableAcceptsTheStatementTheRepositoryActuallyExecutes() throws Exception {
        long gameId = insertGame();

        int updated = jdbcClient.sql(saveUpdateTimeSql())
                .param("gameId", gameId)
                .param("name", "PhysicSystem")
                .param("minInterval", 1_000L)
                .param("maxInterval", 9_000L)
                .param("meanInterval", 4_200.5f)
                .update();

        assertThat(updated).isEqualTo(1);
    }

    @Test
    void storesTheLongestGameSystemNameWithoutTruncation() throws Exception {
        long gameId = insertGame();
        // The longest GameSystem simple name in the server; name is varchar(31) in production, so
        // this is the row closest to the limit that saveUpdateTime can actually write.
        String longestSystemName = GameObjectStateInitialSystem.class.getSimpleName();

        jdbcClient.sql(saveUpdateTimeSql())
                .param("gameId", gameId)
                .param("name", longestSystemName)
                .param("minInterval", 1_000L)
                .param("maxInterval", 9_000L)
                .param("meanInterval", 4_200.5f)
                .update();

        assertThat(readName(gameId)).isEqualTo(longestSystemName);
    }

    @Test
    void storesFrameIntervalsBeyondIntegerMaxValueWithoutTruncation() throws Exception {
        long gameId = insertGame();
        long fourSecondsNs = 4_000_000_000L;

        jdbcClient.sql(saveUpdateTimeSql())
                .param("gameId", gameId)
                .param("name", "Frame")
                .param("minInterval", 50_000_000L)
                .param("maxInterval", fourSecondsNs)
                .param("meanInterval", 5.2e7f)
                .update();

        assertThat(readLong(gameId, "max_interval_ns")).isEqualTo(fourSecondsNs);
        assertThat(readLong(gameId, "min_interval_ns")).isEqualTo(50_000_000L);
        assertThat(readName(gameId)).isEqualTo("Frame");
    }
}
