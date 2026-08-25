package com.wordonline.server.game.service.bot;

import com.wordonline.server.game.domain.bot.BotVisibleObject;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.repository.TagRepository;
import com.wordonline.server.game.service.MagicMetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BotCounterEvaluatorTest {

    private static final List<CardType> RECIPE = List.of(CardType.Spawn, CardType.Fire);
    private static final Set<String> MAGIC_TAGS = Set.of("CAT_Small");
    private static final Set<String> ENEMY_TAGS = Set.of("CAT_AoE");

    private final MagicMetadataService magicMetadataService = mock(MagicMetadataService.class);
    private final TagRepository tagRepository = mock(TagRepository.class);
    private final BotCounterEvaluator evaluator = new BotCounterEvaluator(magicMetadataService, tagRepository);

    private final List<BotVisibleObject> enemies = List.of(enemy());

    @BeforeEach
    void stubMagicLookup() {
        Magic magic = mock(Magic.class);
        magic.id = 7L;
        when(magicMetadataService.findMagic(RECIPE)).thenReturn(Optional.of(magic));
        when(magicMetadataService.getMagicTags(7L)).thenReturn(MAGIC_TAGS);
        when(tagRepository.getGameObjectTags(any(PrefabType.class))).thenReturn(ENEMY_TAGS);
    }

    @Test
    void scoresTheRecipeAsTheAttackerWhenAskedHowMuchItBeatsTheField() {
        when(tagRepository.getCounterWeight(MAGIC_TAGS, ENEMY_TAGS)).thenReturn(3.0);
        when(tagRepository.getCounterWeight(ENEMY_TAGS, MAGIC_TAGS)).thenReturn(0.0);

        assertThat(evaluator.evaluate(RECIPE, enemies)).isEqualTo(3.0);
    }

    // The whole point of the second direction: an AoE board answers a swarm of small units, and the
    // bot that is meant to lose has to be able to see that and walk into it.
    @Test
    void scoresTheEnemyAsTheAttackerWhenAskedHowMuchTheFieldBeatsIt() {
        when(tagRepository.getCounterWeight(MAGIC_TAGS, ENEMY_TAGS)).thenReturn(0.0);
        when(tagRepository.getCounterWeight(ENEMY_TAGS, MAGIC_TAGS)).thenReturn(2.0);

        assertThat(evaluator.evaluateVulnerability(RECIPE, enemies)).isEqualTo(2.0);
    }

    @Test
    void bothDirectionsStayNeutralWhenTheMagicCarriesNoTags() {
        when(magicMetadataService.getMagicTags(7L)).thenReturn(Set.of());

        assertThat(evaluator.evaluate(RECIPE, enemies)).isZero();
        assertThat(evaluator.evaluateVulnerability(RECIPE, enemies)).isZero();
    }

    // The tables this reads did not exist in production until recently, so the swallow is load
    // bearing: a bot must keep playing when counter data is unavailable.
    @Test
    void bothDirectionsFallBackToNeutralWhenTheLookupFails() {
        when(tagRepository.getCounterWeight(any(), any())).thenThrow(new IllegalStateException("no such table"));

        assertThat(evaluator.evaluate(RECIPE, enemies)).isZero();
        assertThat(evaluator.evaluateVulnerability(RECIPE, enemies)).isZero();
    }

    private static BotVisibleObject enemy() {
        return new BotVisibleObject(
                1, Master.RightPlayer, PrefabType.FireSpirit, new Vector3(0, 0, 0),
                Status.Idle, 10, true, true);
    }
}
