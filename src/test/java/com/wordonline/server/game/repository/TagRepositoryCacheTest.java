package com.wordonline.server.game.repository;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The bot scores every affordable recipe against every visible enemy several times a second, so
 * these lookups have to be answered from memory after the first call.
 */
class TagRepositoryCacheTest {

    private static final Set<String> MAGIC_TAGS = Set.of("fire", "projectile");
    private static final Set<String> SLIME_TAGS = Set.of("slime", "ground");

    private CountingTagRepository repository() {
        return new CountingTagRepository(
                Map.of("rock_slime", SLIME_TAGS, "fire_slime", Set.of("fire")),
                Map.of(7L, MAGIC_TAGS),
                2.5
        );
    }

    @Test
    void gameObjectTagsAreLoadedOncePerPrefabType() {
        CountingTagRepository repository = repository();

        Set<String> first = repository.getGameObjectTags(PrefabType.RockSlime);
        Set<String> second = repository.getGameObjectTags(PrefabType.RockSlime);

        assertThat(first).isEqualTo(SLIME_TAGS);
        assertThat(second).isEqualTo(SLIME_TAGS);
        assertThat(repository.gameObjectTagLoads()).containsExactly("rock_slime");
    }

    @Test
    void theEnumAndNameEntryPointsShareOneCacheEntry() {
        CountingTagRepository repository = repository();

        repository.getGameObjectTags(PrefabType.RockSlime);
        repository.getGameObjectTags("rock_slime");

        assertThat(repository.gameObjectTagLoads()).containsExactly("rock_slime");
    }

    @Test
    void differentPrefabTypesStillEachReachTheDatabaseOnce() {
        CountingTagRepository repository = repository();

        repository.getGameObjectTags(PrefabType.RockSlime);
        repository.getGameObjectTags(PrefabType.FireSlime);
        repository.getGameObjectTags(PrefabType.RockSlime);

        assertThat(repository.gameObjectTagLoads()).containsExactly("rock_slime", "fire_slime");
    }

    @Test
    void magicTagsAreLoadedOncePerMagic() {
        CountingTagRepository repository = repository();

        assertThat(repository.getMagicTags(7L)).isEqualTo(MAGIC_TAGS);
        assertThat(repository.getMagicTags(7L)).isEqualTo(MAGIC_TAGS);

        assertThat(repository.magicTagLoads()).containsExactly(7L);
    }

    @Test
    void counterWeightIsKeyedByTheTagSetsNotByTheCollectionsPassedIn() {
        CountingTagRepository repository = repository();

        double first = repository.getCounterWeight(MAGIC_TAGS, SLIME_TAGS);
        // Same tags, different collection type and iteration order: still the same question.
        double second = repository.getCounterWeight(
                List.of("projectile", "fire"),
                new LinkedHashSet<>(List.of("ground", "slime")));

        assertThat(first).isEqualTo(2.5);
        assertThat(second).isEqualTo(2.5);
        assertThat(repository.counterWeightLoads()).hasSize(1);
    }

    @Test
    void distinctTagSetPairsAreScoredSeparately() {
        CountingTagRepository repository = repository();

        repository.getCounterWeight(MAGIC_TAGS, SLIME_TAGS);
        repository.getCounterWeight(MAGIC_TAGS, Set.of("aerial"));

        assertThat(repository.counterWeightLoads()).hasSize(2);
    }

    @Test
    void emptyTagSetsAreAnsweredWithoutTouchingTheDatabase() {
        CountingTagRepository repository = repository();

        assertThat(repository.getCounterWeight(Set.of(), SLIME_TAGS)).isZero();
        assertThat(repository.getCounterWeight(MAGIC_TAGS, Set.of())).isZero();

        assertThat(repository.counterWeightLoads()).isEmpty();
    }

    @Test
    void invalidatingTheCacheMakesTheNextCallReadTheDatabaseAgain() {
        CountingTagRepository repository = repository();

        repository.getGameObjectTags(PrefabType.RockSlime);
        repository.getMagicTags(7L);
        repository.getCounterWeight(MAGIC_TAGS, SLIME_TAGS);

        repository.invalidateCache();

        repository.getGameObjectTags(PrefabType.RockSlime);
        repository.getMagicTags(7L);
        repository.getCounterWeight(MAGIC_TAGS, SLIME_TAGS);

        assertThat(repository.gameObjectTagLoads()).containsExactly("rock_slime", "rock_slime");
        assertThat(repository.magicTagLoads()).containsExactly(7L, 7L);
        assertThat(repository.counterWeightLoads()).hasSize(2);
    }
}
