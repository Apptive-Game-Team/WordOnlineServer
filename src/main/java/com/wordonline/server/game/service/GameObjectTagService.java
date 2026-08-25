package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.bot.view.GameObjectTags;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.repository.TagRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The gameplay tags of every prefab, held in memory for the bot to read.
 *
 * <p>{@link TagRepository#getGameObjectTags(PrefabType)} memoises its own results, but only after
 * the first call for a type has gone to the database. The rule layer asks for the tags of every
 * visible object on every think pass, and a think pass runs on the bot executor while the game loop
 * is running frames, so a first call landing there would put a SQL round trip in the bot's path.
 * {@link PrefabType} is a finite enum, so the whole table is warmed at startup instead and the
 * think path only ever reads a map.
 *
 * <p>Missing data is not an error. A prefab with no tag rows simply has no tags, and if the tag
 * tables are unreachable every prefab has no tags: the bot then scores without tag information
 * rather than failing to play, the same way {@code BotCounterEvaluator} degrades to a neutral
 * score. The tags are reference data edited by admins, so refreshing goes through
 * {@link #invalidateCache()} like {@code MagicMetadataService} and {@code ParameterService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameObjectTagService implements GameObjectTags {

    private final TagRepository tagRepository;
    private final Map<PrefabType, Set<String>> tagsByPrefabType = new ConcurrentHashMap<>();

    @PostConstruct
    void warmUp() {
        try {
            for (PrefabType prefabType : PrefabType.values()) {
                tagsByPrefabType.put(prefabType, Set.copyOf(tagRepository.getGameObjectTags(prefabType)));
            }
            log.info("[GameObjectTags:Loaded] {} prefab types", tagsByPrefabType.size());
        } catch (RuntimeException e) {
            // One log, not one per prefab: if this throws at all it is because the tag tables are
            // unreachable, and that is a single fact about the database, not 99 of them.
            log.warn("[GameObjectTags] Tag data unavailable; bots will score without tags. loaded={}",
                    tagsByPrefabType.size(), e);
        }
    }

    /**
     * The tags of a prefab, or an empty set when it has none or when warm-up could not read them.
     * Never touches the database, so it is safe to call from the bot's think path.
     */
    @Override
    public Set<String> tagsOf(PrefabType prefabType) {
        return getGameObjectTags(prefabType);
    }

    public Set<String> getGameObjectTags(PrefabType prefabType) {
        if (prefabType == null) {
            return Set.of();
        }
        return tagsByPrefabType.getOrDefault(prefabType, Set.of());
    }

    /**
     * Drops the memoised tags, here and in the repository, and reads them again, so an admin edit
     * to the tag tables takes effect without a restart. Also the way a warm-up that found the
     * database down is retried.
     */
    public void invalidateCache() {
        tagRepository.invalidateCache();
        tagsByPrefabType.clear();
        warmUp();
    }
}
