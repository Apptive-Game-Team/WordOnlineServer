package com.wordonline.server.auth.repository;

import com.wordonline.server.auth.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private static final String GET_SELECTED_DECK_ID_BY_ID = """
            SELECT selected_deck_id
            FROM users
            WHERE id = :userId;
            """;

    private static final String UPDATE_STATUS = """
        UPDATE users
           SET status = :status
         WHERE id = :id;
        """;

    private static final String GET_MMR = """
            SELECT mmr
            FROM users
            WHERE id = :userId;
            """;

    private static final String SET_MMR = """
            UPDATE users
            SET mmr = :mmr
            WHERE id = :userId;
            """;

    private static final String INCREMENT_TOTAL_WINS = """
            update users
            set total_wins = coalesce(total_wins, 0) + 1
            where id = :userId;
            """;

    private static final String CLEAR_NOVICE = """
            update users
            set is_novice = false
            where id = :userId
              and is_novice;
            """;

    private final JdbcClient jdbcClient;

    public Optional<Long> getSelectedDeckId(long userId) {
        return jdbcClient.sql(GET_SELECTED_DECK_ID_BY_ID)
                .param("userId", userId)
                .query(Long.class)
                .optional();
    }

    public Optional<Short> getMmr(long userId) {
        return jdbcClient.sql(GET_MMR)
                .param("userId", userId)
                .query(Short.class)
                .optional();
    }

    public void setMmr(long userId, short mmr) {
        jdbcClient.sql(SET_MMR)
                .param("userId", userId)
                .param("mmr", mmr)
                .update();
    }

    public void updateStatus(long userId, UserStatus status) {
        jdbcClient.sql(UPDATE_STATUS)
                .param("id", userId)
                .param("status", status.name())
                .update();
    }

    public void incrementTotalWins(long userId) {
        jdbcClient.sql(INCREMENT_TOTAL_WINS)
                .param("userId", userId)
                .update();
    }

    /** @return true when this call was the one that cleared the flag. */
    public boolean clearNovice(long userId) {
        return jdbcClient.sql(CLEAR_NOVICE)
                .param("userId", userId)
                .update() > 0;
    }
}
