package com.wordonline.server.game.domain.bot;

import com.wordonline.server.bot.domain.BotPersona;
import com.wordonline.server.bot.domain.BotTier;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.bot.rule.BotRuleBook;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.implement.explode.WaterExplosionMagic;
import com.wordonline.server.game.domain.magic.implement.explode.WindExplosionMagic;
import com.wordonline.server.game.domain.magic.implement.shoot.VineTossMagic;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.bot.BotCounterEvaluator;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BotBrainComboTest {

    @Test
    void seedSpiritComboPrecedesTheExistingValueDecision() {
        Map<List<CardType>, Magic> recipes = new LinkedHashMap<>();
        recipes.put(List.of(CardType.Nature, CardType.Shoot), new VineTossMagic());
        recipes.put(List.of(CardType.Water, CardType.Explode), new WaterExplosionMagic());
        BotBrain brain = brain(recipes, BotTier.ELITE);
        Vector3 seedPosition = new Vector3(4, 0, 5);
        BotEye eye = new BotEye(
                List.of(
                        visible(1, Master.LeftPlayer, PrefabType.SeedSpirit, seedPosition, true),
                        visible(2, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(6, 0, 4), true),
                        visible(3, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(6, 0, 5), true),
                        visible(4, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(6, 0, 6), true)),
                List.of(CardType.Nature, CardType.Shoot, CardType.Water, CardType.Explode), 20, 100);

        BotBrain.InputDecision decision = brain.think(eye, parameters(), Master.LeftPlayer, new Random(1));

        assertThat(decision.ruleId()).isEqualTo(BotRuleBook.SEED_SPIRIT_RULE_ID);
        assertThat(decision.playCards()).containsExactly(CardType.Nature, CardType.Shoot);
        assertThat(decision.target()).isEqualTo(seedPosition);
    }

    @Test
    void explosionComboTargetsTheAverageOfThreeNearbyEnemyMobs() {
        Map<List<CardType>, Magic> recipes = Map.of(
                List.of(CardType.Water, CardType.Explode), new WaterExplosionMagic());
        BotBrain brain = brain(recipes, BotTier.ELITE);
        BotEye eye = new BotEye(
                List.of(
                        visible(2, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(5, 0, 4), true),
                        visible(3, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(5, 0, 5), true),
                        visible(4, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(5, 0, 6), true)),
                List.of(CardType.Water, CardType.Explode), 20, 100);

        BotBrain.InputDecision decision = brain.think(eye, parameters(), Master.LeftPlayer, new Random(1));

        assertThat(decision.ruleId()).isEqualTo(BotRuleBook.MOB_CLUSTER_RULE_ID);
        assertThat(decision.target()).isEqualTo(new Vector3(5, 0, 5));
        assertThat(decision.reason()).contains("3 enemy mobs");
    }

    @Test
    void appliesTierNoiseExactlyOncePerScoredCandidate() {
        Map<List<CardType>, Magic> recipes = new LinkedHashMap<>();
        recipes.put(List.of(CardType.Water, CardType.Explode), new WaterExplosionMagic());
        recipes.put(List.of(CardType.Wind, CardType.Explode), new WindExplosionMagic());
        BotBrain brain = brain(recipes, BotTier.BEGINNER);
        BotEye eye = new BotEye(
                List.of(visible(2, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(5, 0, 5), true)),
                List.of(CardType.Water, CardType.Wind, CardType.Explode, CardType.Explode), 20, 100);
        CountingRandom random = new CountingRandom();

        BotBrain.InputDecision decision = brain.think(eye, parameters(), Master.LeftPlayer, random);

        assertThat(decision).isNotNull();
        assertThat(random.nextDoubleCalls).isEqualTo(2);
    }

    private static BotBrain brain(Map<List<CardType>, Magic> recipes, BotTier tier) {
        DatabaseMagicParser parser = mock(DatabaseMagicParser.class);
        when(parser.getAllMagicRecipeMap()).thenReturn(recipes);
        BotCounterEvaluator counterEvaluator = mock(BotCounterEvaluator.class);
        when(counterEvaluator.evaluate(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(0.0);
        BotPersona persona = new BotPersona(1, "test", tier, 0, 1, 0, true, false);
        return new BotBrain(parser, counterEvaluator, persona);
    }

    private static Parameters parameters() {
        Parameters parameters = mock(Parameters.class);
        when(parameters.getValueOrDefault(anyString(), anyString(), anyDouble())).thenAnswer(invocation -> {
            String key = invocation.getArgument(1);
            return switch (key) {
                case "mana_cost" -> 1.0;
                case "range" -> 20.0;
                case "radius" -> 1.0;
                case "damage" -> 10.0;
                default -> invocation.getArgument(2);
            };
        });
        return parameters;
    }

    private static BotVisibleObject visible(int id,
                                            Master master,
                                            PrefabType type,
                                            Vector3 position,
                                            boolean mob) {
        return new BotVisibleObject(id, master, type, position, Status.Idle, 10, mob, true);
    }

    private static final class CountingRandom extends Random {
        private int nextDoubleCalls;

        @Override
        public double nextDouble() {
            nextDoubleCalls++;
            return 0.5;
        }
    }
}
