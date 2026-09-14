package com.wordonline.server.game.repository;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * 마법별 시전 값을 읽는다. {@link ParameterRepository} 가 game object 의 값을 읽는 것과 같은
 * 모양이고, 대상이 game object 가 아니라 마법이라는 점만 다르다.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MagicParameterRepository {

    private final JdbcClient jdbcClient;

    private static final String GET_MAGIC_PARAMETER_VALUE = """
            SELECT value
            FROM magic_parameters
            JOIN parameters ON magic_parameters.parameter_id = parameters.id
            WHERE magic_parameters.magic_id = :magicId AND parameters.name = :parameter;
            """;

    public Optional<Double> getParameterValue(long magicId, String parameter) {
        log.debug("[Database] get magic parameter magic: {} | parameter: {}", magicId, parameter);
        return jdbcClient.sql(GET_MAGIC_PARAMETER_VALUE)
                .param("magicId", magicId)
                .param("parameter", parameter)
                .query(Double.class)
                .optional();
    }
}
