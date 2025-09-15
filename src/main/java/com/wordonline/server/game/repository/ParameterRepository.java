package com.wordonline.server.game.repository;

import java.util.Optional;
import java.util.OptionalDouble;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ParameterRepository {

    private final JdbcClient jdbcClient;

    private static final String GET_PARAMETER_VALUE = """
            SELECT value
            FROM parameter_values
            JOIN game_objects ON parameter_values.game_object_id = game_objects.id
            JOIN parameters ON parameter_values.parameter_id = parameters.id
            WHERE game_objects.name = :gameObject AND parameters.name = :parameter;
            """;

    public Optional<Double> getParameterValue(String gameObject, String parameter) {
        log.info("[Database] get parameter gameobject: {} | parameter: {}", gameObject.toLowerCase(), parameter);
        return jdbcClient.sql(GET_PARAMETER_VALUE)
                .param("gameObject", gameObject.toLowerCase())
                .param("parameter", parameter)
                .query(Double.class)
                .optional();
    }
}
