package com.wordonline.server.game.repository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ParameterRepository {

    private final JdbcClient jdbcClient;

    private static final long DEFAULT_PARAMETER_PROFILE_ID = 1L;

    private static final String GET_PARAMETER_VALUE = """
            SELECT value
            FROM parameter_values
            JOIN game_objects ON parameter_values.game_object_id = game_objects.id
            JOIN parameters ON parameter_values.parameter_id = parameters.id
            WHERE parameter_values.parameter_profile_id = :parameterProfileId
              AND game_objects.name = :gameObject
              AND parameters.name = :parameter;
            """;

    private static final String GET_PROFILE_PARAMETER_VALUE = """
            SELECT parameter_values.value
            FROM parameter_values
            JOIN game_objects ON parameter_values.game_object_id = game_objects.id
            JOIN parameters ON parameter_values.parameter_id = parameters.id
            WHERE parameter_values.parameter_profile_id = :parameterProfileId
              AND game_objects.name = :gameObject
              AND parameters.name = :parameter;
            """;

    private static final String GET_PARENT_PROFILE_ID = """
            SELECT parent_profile_id
            FROM parameter_profiles
            WHERE id = :parameterProfileId;
            """;

    public Optional<Double> getParameterValue(String gameObject, String parameter) {
        log.info("[Database] get parameter gameobject: {} | parameter: {}", gameObject.toLowerCase(), parameter);
        return jdbcClient.sql(GET_PARAMETER_VALUE)
                .param("parameterProfileId", DEFAULT_PARAMETER_PROFILE_ID)
                .param("gameObject", gameObject.toLowerCase())
                .param("parameter", parameter)
                .query(Double.class)
                .optional();
    }

    public Optional<Double> getParameterValue(String gameObject, String parameter, Long parameterProfileId) {
        if (parameterProfileId == null) {
            return getParameterValue(gameObject, parameter);
        }

        Optional<Double> profileValue = getProfileParameterValue(gameObject, parameter, parameterProfileId);
        if (profileValue.isPresent()) {
            return profileValue;
        }
        return getProfileParameterValue(gameObject, parameter, DEFAULT_PARAMETER_PROFILE_ID);
    }

    private Optional<Double> getProfileParameterValue(String gameObject, String parameter, Long parameterProfileId) {
        Set<Long> visitedProfileIds = new HashSet<>();
        Long currentProfileId = parameterProfileId;

        while (currentProfileId != null && visitedProfileIds.add(currentProfileId)) {
            Optional<Double> value = jdbcClient.sql(GET_PROFILE_PARAMETER_VALUE)
                    .param("parameterProfileId", currentProfileId)
                    .param("gameObject", gameObject.toLowerCase())
                    .param("parameter", parameter)
                    .query(Double.class)
                    .optional();
            if (value.isPresent()) {
                return value;
            }

            currentProfileId = jdbcClient.sql(GET_PARENT_PROFILE_ID)
                    .param("parameterProfileId", currentProfileId)
                    .query(Long.class)
                    .optional()
                    .orElse(null);
        }

        return Optional.empty();
    }
}
