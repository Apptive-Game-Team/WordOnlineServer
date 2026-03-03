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

    private final static String FIND_ALL = """
            SELECT mmc.magic_id AS id, mmc.name AS name, STRING_AGG(c.name, ',') AS cards
            FROM (magics m
            JOIN magic_cards mc ON m.id = mc.magic_id) AS mmc
            JOIN cards c ON mmc.card_id = c.id
            GROUP BY mmc.magic_id, mmc.name;
            """;

    public boolean existUserMagic(long userId, long magicId) {
        return jdbcClient.sql(USER_MAGIC_EXIST)
                .param("userId", userId)
                .param("magicId", magicId)
                .query()
                .singleValue() instanceof Boolean result && result;
    }

    public List<MagicInfoDto> getAllMagic() {
        return jdbcClient.sql(FIND_ALL)
                .query((rs, num) -> new MagicInfoDto(
                            rs.getLong("id"),
                            rs.getString("name"),
                            Arrays.stream(rs.getString("cards").split(","))
                                    .map(CardType::valueOf)
                                    .toList()
                )).list();
    }
}
