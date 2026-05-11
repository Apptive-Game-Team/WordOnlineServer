package com.wordonline.server.game.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ParameterRepository.class)
class ParameterRepositoryTest {

    @Autowired
    private ParameterRepository parameterRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void setUpProfiles() {
        jdbcClient.sql("""
                INSERT INTO parameter_profiles(id, name, parent_profile_id)
                VALUES (100, 'parent-profile', NULL), (101, 'child-profile', 100)
                """).update();

        jdbcClient.sql("""
                INSERT INTO parameter_values(parameter_profile_id, parameter_id, game_object_id, value)
                VALUES
                    (100, 2, 1, 7.0),
                    (101, 3, 1, 0.9)
                """).update();
    }

    @Test
    void shouldResolveProfileOverrideAndFallbacks() {
        assertThat(parameterRepository.getParameterValue("slime", "radius", 101L))
                .contains(0.9);
        assertThat(parameterRepository.getParameterValue("slime", "damage", 101L))
                .contains(7.0);
        assertThat(parameterRepository.getParameterValue("slime", "hp", 101L))
                .contains(8.0);
    }
}
