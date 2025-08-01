package com.wordonline.server.auth.repository;

import com.wordonline.server.auth.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private static final String GET_USER_BY_EMAIL = """
            SELECT id, email, name, password_hash
            FROM users
            WHERE email = :email;
            """;
    private static final String GET_USER_BY_ID = """
            SELECT id, email, name, password_hash
            FROM users
            WHERE id = :id;
            """;
    private static final String SAVE_USER = """
            INSERT INTO users(email, name, password_hash)
            VALUES
            (:email, :name, :passwordHash);
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
                                rs.getString("email"),
                                rs.getString("name"),
                                rs.getString("password_hash")
                        ))
                .optional();
    }

    public Optional<User> findUserById(long id) {
        return jdbcClient.sql(GET_USER_BY_ID)
                .param("id", id)
                .query((rs, num) ->
                        new User(
                                rs.getLong("id"),
                                rs.getString("email"),
                                rs.getString("name"),
                                rs.getString("password_hash")
                        ))
                .optional();
    }

    public boolean saveUser(User user) {
        return jdbcClient.sql(SAVE_USER)
                .param("email", user.getEmail())
                .param("name", user.getName())
                .param("passwordHash", user.getPasswordHash())
                .update() == 1;
    }
}
