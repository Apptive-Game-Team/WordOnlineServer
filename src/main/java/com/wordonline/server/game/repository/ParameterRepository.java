package com.wordonline.server.game.repository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

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

    public Double getParameterValue(String gameObject, String parameter) {
        log.info("get parameter gameobject: {} | parameter: {}", gameObject, parameter);
        return jdbcClient.sql(GET_PARAMETER_VALUE)
                .param("gameObject", gameObject)
                .param("parameter", parameter)
                .query(Double.class)
                .single();
    }

    private static final Logger log = LoggerFactory.getLogger(ParameterRepository.class);
}
