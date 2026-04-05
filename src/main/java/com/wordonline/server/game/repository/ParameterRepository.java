package com.wordonline.server.game.repository;

import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private static final String GET_ALL_PARAMETER_VALUES = """
            SELECT game_objects.name AS game_object, parameters.name AS parameter, value
            FROM parameter_values
            JOIN game_objects ON parameter_values.game_object_id = game_objects.id
            JOIN parameters ON parameter_values.parameter_id = parameters.id;
            """;

    public Optional<Double> getParameterValue(String gameObject, String parameter) {
        log.info("[Database] get parameter gameobject: {} | parameter: {}", gameObject.toLowerCase(), parameter);
        return jdbcClient.sql(GET_PARAMETER_VALUE)
                .param("gameObject", gameObject.toLowerCase())
                .param("parameter", parameter)
                .query(Double.class)
                .optional();
    }

    public Map<String, Map<String, Double>> getAllParameterValues() {
        Map<String, Map<String, Double>> result = new java.util.HashMap<>();
        jdbcClient.sql(GET_ALL_PARAMETER_VALUES)
                .query((rs, rowNum) -> {
                    result
                            .computeIfAbsent(rs.getString("game_object"), k -> new java.util.HashMap<>())
                            .put(rs.getString("parameter"), rs.getDouble("value"));
                    return null;
                })
                .list();
        return result;
    }
}
