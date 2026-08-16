package com.wordonline.server.statistic.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.statistic.domain.GameSessionStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GameSessionRecordRepository {

    private final JdbcClient jdbcClient;

    private static final String INSERT_SESSION = """
            INSERT INTO statistic_game_sessions(
                session_id,
                left_user_id,
                right_user_id,
                game_type,
                server_domain,
                server_port,
                server_instance_id,
                server_version
            )
            VALUES(
                :sessionId,
                :leftUserId,
                :rightUserId,
                :gameType::game_type,
                :serverDomain,
                :serverPort,
                :serverInstanceId,
                :serverVersion
            ) RETURNING id;
            """;

    // The status guard keeps a late writer (a zombie loop thread waking up after the
    // watchdog already resolved the row) from overwriting the recorded end.
    private static final String MARK_ENDED = """
            UPDATE statistic_game_sessions
            SET status = :status,
                end_reason = :endReason,
                end_detail = :endDetail,
                statistic_game_id = :statisticGameId,
                ended_at = now()
            WHERE id = :id AND status = 'IN_PROGRESS';
            """;

    // instance_id <> current: rows from a previous process of the same deployment. The
    // process that wrote them is gone, so nothing else will ever resolve them.
    private static final String ABANDON_LEFTOVERS = """
            UPDATE statistic_game_sessions
            SET status = 'ABANDONED',
                end_reason = :endReason,
                ended_at = now()
            WHERE server_domain = :serverDomain
                AND server_port = :serverPort
                AND server_instance_id <> :currentInstanceId
                AND status = 'IN_PROGRESS';
            """;

    public long insert(String sessionId,
                       long leftUserId,
                       long rightUserId,
                       SessionType sessionType,
                       String serverDomain,
                       int serverPort,
                       String serverInstanceId,
                       String serverVersion) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(INSERT_SESSION)
                .param("sessionId", sessionId)
                .param("leftUserId", leftUserId)
                .param("rightUserId", rightUserId)
                .param("gameType", sessionType.name())
                .param("serverDomain", serverDomain)
                .param("serverPort", serverPort)
                .param("serverInstanceId", serverInstanceId)
                .param("serverVersion", serverVersion)
                .update(keyHolder);
        return keyHolder.getKey().longValue();
    }

    public boolean markEnded(long id, GameSessionStatus status, String endReason, String endDetail, Long statisticGameId) {
        return jdbcClient.sql(MARK_ENDED)
                .param("id", id)
                .param("status", status.name())
                .param("endReason", endReason)
                .param("endDetail", endDetail)
                .param("statisticGameId", statisticGameId)
                .update() > 0;
    }

    public int abandonLeftovers(String serverDomain, int serverPort, String currentInstanceId, String endReason) {
        return jdbcClient.sql(ABANDON_LEFTOVERS)
                .param("serverDomain", serverDomain)
                .param("serverPort", serverPort)
                .param("currentInstanceId", currentInstanceId)
                .param("endReason", endReason)
                .update();
    }
}
