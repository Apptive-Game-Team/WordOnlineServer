package com.wordonline.server.statistic.repository;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 * Builds an H2 database containing only the statistic tables, read out of the real schema-h2.sql so
 * the definitions under test are the shipped ones.
 * <p>
 * This is the approach {@link StatisticUpdateTimeSchemaTest} established. The whole script still
 * cannot be executed on H2: {@code parameter_values.value} uses a reserved word and the
 * {@code ALTER TABLE ... DROP CONSTRAINT ..., ADD CONSTRAINT ...} statements are Postgres-only
 * multi-action forms whose constraint names H2 never generated. That is a pre-existing gap wider
 * than these tests, so it is worked around here rather than papered over.
 */
final class StatisticSchemaFixture {

    private StatisticSchemaFixture() {
    }

    static JdbcClient createStatisticTables(String databaseName) throws Exception {
        DataSource dataSource = new SimpleDriverDataSource(
                new org.h2.Driver(),
                "jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
        JdbcClient jdbcClient = JdbcClient.create(dataSource);
        List<String> ddl = statisticDdl();
        // Fail loudly rather than silently testing nothing if a definition goes missing.
        if (ddl.size() != 4) {
            throw new IllegalStateException("expected 4 statistic DDL statements, found " + ddl.size() + ": " + ddl);
        }
        ddl.forEach(statement -> jdbcClient.sql(statement).update());
        return jdbcClient;
    }

    private static List<String> statisticDdl() throws Exception {
        String script = new ClassPathResource("schema-h2.sql")
                .getContentAsString(StandardCharsets.UTF_8);
        // Drop comment lines first: they may contain semicolons and would break naive splitting.
        String statements = script.lines()
                .filter(line -> !line.trim().startsWith("--"))
                .collect(Collectors.joining("\n"));
        return Arrays.stream(statements.split(";"))
                .map(String::trim)
                // Deliberately narrow: statements merely referencing statistic_games, such as the
                // card and magic tables, would drag in tables these tests do not create.
                .filter(statement -> statement.contains("CREATE TABLE statistic_games")
                        || statement.contains("CREATE TABLE statistic_update_time")
                        || statement.contains("ON statistic_games")
                        || statement.contains("ON statistic_update_time"))
                .toList();
    }
}
