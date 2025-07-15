package com.wordonline.server.auth.repository;

import com.wordonline.server.auth.domain.KakaoUser;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private static final String GET_USER_BY_EMAIL = """
            SELECT id, email, name, profile_image_url
            FROM users
            WHERE email = :email;
            """;
    private static final String GET_USER_BY_ID = """
            SELECT id, email, name, profile_image_url
            FROM users
            WHERE id = :id;
            """;
    private static final String SAVE_USER = """
            INSERT INTO users(id, email, name, profile_image_url)
            VALUES
            (:id, :email, :name, :image_url);
            """;

    private final JdbcClient jdbcClient;

    public Optional<KakaoUser> findUserByEmail(String email) {
        return jdbcClient.sql(GET_USER_BY_EMAIL)
                .param("email", email)
                .query((rs, num) ->
                        new KakaoUser(
                                rs.getLong("id"),
                                rs.getString("email"),
                                rs.getString("name"),
                                rs.getString("profile_image_url")
                        ))
                .optional();
    }

    public Optional<KakaoUser> findUserById(long id) {
        return jdbcClient.sql(GET_USER_BY_ID)
                .param("id", id)
                .query((rs, num) ->
                        new KakaoUser(
                                rs.getLong("id"),
                                rs.getString("email"),
                                rs.getString("name"),
                                rs.getString("profile_image_url")
                        ))
                .optional();
    }

    public boolean saveUser(KakaoUser kakaoUser) {
        return jdbcClient.sql(SAVE_USER)
                .param("id", kakaoUser.id())
                .param("email", kakaoUser.email())
                .param("name", kakaoUser.nickname())
                .param("image_url", kakaoUser.imageUrl())
                .update() == 1;
    }
}
