package com.wordonline.server.game.repository;

import java.util.Arrays;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.dto.MagicInfoDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MagicRepository {

    private final JdbcClient jdbcClient;

    private final static String USER_MAGIC_EXIST = """
            SELECT EXISTS (
                 SELECT 1
                 FROM user_magics
                 WHERE user_id = :userId
                   AND magic_id = :magicId
             );
            """;

    /**
     * 마법이 prefab 이름을 직접 들지 않고 game object 를 가리키는 이유는 셋이 1:1 이 아니기
     * 때문이다 — 마법 {@code ember_spirit_swarm} 은 game object {@code ember_spirit} 에서
     * quantity 를 읽고 prefab {@code EmberSpirit} 을 만든다. 그래서 이 조회가 마법 →
     * game object → prefab 을 한 줄로 잇는다.
     *
     * <p>game object 를 LEFT JOIN 으로 붙이는 것은 {@code game_object_id} 가 비어 있는 마법도
     * 지금처럼 bean 으로 등록되어야 하기 때문이다.
     */
    private final static String FIND_ALL = """
            SELECT m.id AS id,
                   m.name AS name,
                   STRING_AGG(c.name, ',') AS cards,
                   m.cast_kind AS cast_kind,
                   go.name AS game_object_name,
                   go.prefab AS prefab
            FROM magics m
            JOIN magic_cards mc ON m.id = mc.magic_id
            JOIN cards c ON mc.card_id = c.id
            LEFT JOIN game_objects go ON m.game_object_id = go.id
            GROUP BY m.id, m.name, m.cast_kind, go.name, go.prefab;
            """;

    public boolean existUserMagic(long userId, long magicId) {
        return jdbcClient.sql(USER_MAGIC_EXIST)
                .param("userId", userId)
                .param("magicId", magicId)
                .query(Boolean.class)
                .optional()
                .orElse(false);
    }

    public List<MagicInfoDto> getAllMagic() {
        return jdbcClient.sql(FIND_ALL)
                .query((rs, num) -> new MagicInfoDto(
                            rs.getLong("id"),
                            rs.getString("name"),
                            Arrays.stream(rs.getString("cards").split(","))
                                    .map(CardType::valueOf)
                                    .toList(),
                            rs.getString("cast_kind"),
                            rs.getString("game_object_name"),
                            rs.getString("prefab")
                )).list();
    }
}
