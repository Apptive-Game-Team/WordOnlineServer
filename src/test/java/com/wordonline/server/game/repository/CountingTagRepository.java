package com.wordonline.server.game.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TagRepository with the SQL replaced by fixtures and a counter, so tests can tell a cache hit from
 * a database round trip without standing up a datasource.
 */
public class CountingTagRepository extends TagRepository {

    private final Map<String, Set<String>> gameObjectTagsByName;
    private final Map<Long, Set<String>> magicTagsById;
    private final double counterWeight;

    private final List<String> gameObjectTagLoads = new ArrayList<>();
    private final List<Long> magicTagLoads = new ArrayList<>();
    private final List<List<Set<String>>> counterWeightLoads = new ArrayList<>();

    public CountingTagRepository(Map<String, Set<String>> gameObjectTagsByName,
                                 Map<Long, Set<String>> magicTagsById,
                                 double counterWeight) {
        super(null);
        this.gameObjectTagsByName = gameObjectTagsByName;
        this.magicTagsById = magicTagsById;
        this.counterWeight = counterWeight;
    }

    @Override
    protected Set<String> loadGameObjectTags(String gameObjectName) {
        gameObjectTagLoads.add(gameObjectName);
        return gameObjectTagsByName.getOrDefault(gameObjectName, Set.of());
    }

    @Override
    protected Set<String> loadMagicTags(long magicId) {
        magicTagLoads.add(magicId);
        return magicTagsById.getOrDefault(magicId, Set.of());
    }

    @Override
    protected double loadCounterWeight(Collection<String> attackerTags, Collection<String> targetTags) {
        counterWeightLoads.add(List.of(Set.copyOf(attackerTags), Set.copyOf(targetTags)));
        return counterWeight;
    }

    public List<String> gameObjectTagLoads() {
        return gameObjectTagLoads;
    }

    public List<Long> magicTagLoads() {
        return magicTagLoads;
    }

    public List<List<Set<String>>> counterWeightLoads() {
        return counterWeightLoads;
    }
}
