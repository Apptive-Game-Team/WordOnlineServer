package com.wordonline.server.game.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserScenarioRepository {

    private static final String UPDATE_STATE = """
            UPDATE user_scenarios
            SET state = :state
            WHERE user_id = :userId
              AND scenario_id = :scenarioId;
            """;

    private final JdbcClient jdbcClient;

    public int updateState(long userId, long scenarioId, String state) {
        return jdbcClient.sql(UPDATE_STATE)
                .param("userId", userId)
                .param("scenarioId", scenarioId)
                .param("state", state)
                .update();
    }
}
