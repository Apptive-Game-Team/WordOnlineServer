package com.wordonline.server.game.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.h2.Driver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

class MagicParameterRepositoryTest {

    private JdbcClient jdbcClient;
    private MagicParameterRepository magicParameterRepository;

    @BeforeEach
    void setUp() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource(
                new Driver(),
                "jdbc:h2:mem:magic_parameter_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1;NON_KEYWORDS=VALUE",
                "sa",
                ""
        );
        jdbcClient = JdbcClient.create(dataSource);
        jdbcClient.sql("CREATE TABLE parameters (id BIGINT PRIMARY KEY, name VARCHAR(31))").update();
        jdbcClient.sql("""
                CREATE TABLE magic_parameters (
                    id BIGINT PRIMARY KEY,
                    magic_id BIGINT,
                    parameter_id BIGINT,
                    value DOUBLE PRECISION
                )
                """).update();
        jdbcClient.sql("INSERT INTO parameters (id, name) VALUES (1, 'spawn_height')").update();
        jdbcClient.sql("INSERT INTO parameters (id, name) VALUES (2, 'quantity')").update();
        jdbcClient.sql("""
                INSERT INTO magic_parameters (id, magic_id, parameter_id, value) VALUES (1, 55, 1, 3.0)
                """).update();
        magicParameterRepository = new MagicParameterRepository(jdbcClient);
    }

    @Test
    void readsTheValueStoredForThatMagic() {
        assertThat(magicParameterRepository.getParameterValue(55, "spawn_height")).contains(3.0);
    }

    @Test
    void aParameterWithNoRowForThatMagicIsEmpty() {
        assertThat(magicParameterRepository.getParameterValue(55, "quantity")).isEmpty();
        assertThat(magicParameterRepository.getParameterValue(56, "spawn_height")).isEmpty();
    }
}
