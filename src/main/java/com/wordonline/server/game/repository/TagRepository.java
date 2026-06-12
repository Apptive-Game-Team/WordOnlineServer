package com.wordonline.server.game.repository;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class TagRepository {

    private static final String FIND_MAGIC_TAGS = """
            SELECT t.name
            FROM magic_tags mt
            JOIN tags t ON t.id = mt.tag_id
            WHERE mt.magic_id = :magicId;
            """;

    private static final String FIND_GAME_OBJECT_TAGS = """
            SELECT t.name
            FROM game_object_tags got
            JOIN game_objects go ON go.id = got.game_object_id
            JOIN tags t ON t.id = got.tag_id
            WHERE go.name = :gameObjectName;
            """;

    private static final String COUNTER_WEIGHT = """
            SELECT COALESCE(SUM(tcr.weight), 0)
            FROM tag_counter_rules tcr
            JOIN tags attacker ON attacker.id = tcr.attacker_tag_id
            JOIN tags target ON target.id = tcr.target_tag_id
            WHERE attacker.name IN (:attackerTags)
              AND target.name IN (:targetTags);
            """;

    private final JdbcClient jdbcClient;

    public Set<String> getMagicTags(long magicId) {
        return Set.copyOf(jdbcClient.sql(FIND_MAGIC_TAGS)
                .param("magicId", magicId)
                .query(String.class)
                .list());
    }

    public Set<String> getGameObjectTags(PrefabType prefabType) {
        return getGameObjectTags(toGameObjectName(prefabType));
    }

    public Set<String> getGameObjectTags(String gameObjectName) {
        return Set.copyOf(jdbcClient.sql(FIND_GAME_OBJECT_TAGS)
                .param("gameObjectName", gameObjectName)
                .query(String.class)
                .list());
    }

    public double getCounterWeight(Collection<String> attackerTags, Collection<String> targetTags) {
        if (attackerTags.isEmpty() || targetTags.isEmpty()) {
            return 0.0;
        }
        return jdbcClient.sql(COUNTER_WEIGHT)
                .param("attackerTags", List.copyOf(attackerTags))
                .param("targetTags", List.copyOf(targetTags))
                .query(Double.class)
                .optional()
                .orElse(0.0);
    }

    private static String toGameObjectName(PrefabType prefabType) {
        String name = prefabType.name();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                builder.append('_');
            }
            builder.append(Character.toLowerCase(c));
        }
        return builder.toString();
    }
}
