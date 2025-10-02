package com.wordonline.server.auth.repository;

import com.wordonline.server.auth.domain.User;
import com.wordonline.server.auth.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private static final String DELETE_USER_BY_ID = """
            DELETE FROM users
            WHERE id = :id;
            """;
    private static final String GET_USER_BY_EMAIL = """
            SELECT id, name, status, selected_deck_id

            FROM users
            WHERE email = :email;
            """;
    private static final String GET_USER_BY_ID = """
            SELECT id, name, status, selected_deck_id
            FROM users
            WHERE id = :id;
            """;
    private static final String SAVE_USER = """
            INSERT INTO users(id, email, name, password_hash)
            VALUES
            (:id, :email, :name, :passwordHash);
            """;

    private static final String UPDATE_STATUS = """
        UPDATE users
           SET status = CAST(:status AS user_status)
         WHERE id = :id;
        """;

    private static final String GET_STATUS = """
        SELECT status
          FROM users
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

    private final JdbcClient jdbcClient;

    public boolean deleteById(long userId) {
        return jdbcClient.sql(DELETE_USER_BY_ID)
                .param("id", userId)
                .update() >= 1;
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

    public Optional<User> findUserByEmail(String email) {
        return jdbcClient.sql(GET_USER_BY_EMAIL)
                .param("email", email)
                .query((rs, num) ->
                        new User(
                                rs.getLong("id"),
                                rs.getString("name"),
                                UserStatus.valueOf(rs.getString("status")),
                                rs.getLong("selected_deck_id")
                        ))
                .optional();
    }

    public Optional<User> findUserById(long id) {
        return jdbcClient.sql(GET_USER_BY_ID)
                .param("id", id)
                .query((rs, num) ->
                        new User(
                                rs.getLong("id"),
                                rs.getString("name"),
                                UserStatus.valueOf(rs.getString("status")),
                                rs.getLong("selected_deck_id")
                        ))
                .optional();
    }

    public void saveUser(long userId) {
        String uniqueEmail = "user_" + System.currentTimeMillis() + UUID.randomUUID() + "@example.com";
        String name = "user_" + System.currentTimeMillis();
        jdbcClient.sql(SAVE_USER)
                .param("id", userId)
                .param("email", uniqueEmail)
                .param("name", name)
                .param("passwordHash", "")
                .update();
    }

    public void updateStatus(long userId, UserStatus status) {
        jdbcClient.sql(UPDATE_STATUS)
                .param("id", userId)
                .param("status", status.name())
                .update();
    }

    public UserStatus getStatus(long userId) {
        return jdbcClient.sql(GET_STATUS)
                .param("id", userId)
                .query((rs, rowNum) ->
                        UserStatus.valueOf(rs.getString("status"))
                )
                .single();
    }
}
