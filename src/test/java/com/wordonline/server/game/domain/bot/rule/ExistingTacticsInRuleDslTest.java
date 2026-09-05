package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.bot.view.BotWorldView;
import com.wordonline.server.game.domain.magic.implement.explode.AbstractExplosionMagic;
import com.wordonline.server.game.domain.magic.implement.explode.MagmaExplosionMagic;
import com.wordonline.server.game.domain.magic.implement.explode.OvergrowthMagic;
import com.wordonline.server.game.domain.magic.implement.shoot.FireShotMagic;
import com.wordonline.server.game.domain.magic.implement.shoot.VineTossMagic;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.BOT_SIDE;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.ENEMY_SIDE;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.SELF;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.cluster;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.object;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.option;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.view;
import static com.wordonline.server.game.domain.bot.rule.BotRules.ONE_PER_MANA;
import static com.wordonline.server.game.domain.bot.rule.BotRules.ally;
import static com.wordonline.server.game.domain.bot.rule.BotRules.clusterCenter;
import static com.wordonline.server.game.domain.bot.rule.BotRules.clusterSizePerMana;
import static com.wordonline.server.game.domain.bot.rule.BotRules.enemyCluster;
import static com.wordonline.server.game.domain.bot.rule.BotRules.matchedTarget;
import static com.wordonline.server.game.domain.bot.rule.BotRules.rule;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The two tactics {@code BotBrain} currently hard-codes, written in the rule vocabulary.
 *
 * <p>This is the proof that the vocabulary is sufficient before anything is wired: both tactics are
 * expressible as declarations, and each produces the same casts, the same scores and the same
 * explanations the hand-written versions produced. The rule book itself belongs to the coordinator;
 * these declarations are only here to be checked against the behaviour they replace.
 */
class ExistingTacticsInRuleDslTest {

    private static final BotRule SEED_SPIRIT_COMBO =
            rule("combo.seed-spirit")
                    .priority(100)
                    .castingMagic(VineTossMagic.class, OvergrowthMagic.class)
                    .when(ally(PrefabType.SeedSpirit).inCastRange())
                    .at(matchedTarget())
                    .scoring(ONE_PER_MANA);

    private static final BotRule MOB_CLUSTER_COMBO =
            rule("combo.mob-cluster")
                    .priority(90)
                    .castingMagic(AbstractExplosionMagic.class)
                    .when(enemyCluster().minSize(3).inCastRange())
                    .at(clusterCenter())
                    .scoring(clusterSizePerMana());

    private static final RecipeOption VINE_TOSS = option(new VineTossMagic(), 4, 8);
    private static final RecipeOption OVERGROWTH = option(new OvergrowthMagic(), 6, 8);
    private static final RecipeOption MAGMA = option(new MagmaExplosionMagic(), 5, 8);
    private static final RecipeOption FIRE_SHOT = option(new FireShotMagic(), 2, 8);

    private final PriorityBotRuleEngine engine =
            new PriorityBotRuleEngine(List.of(SEED_SPIRIT_COMBO, MOB_CLUSTER_COMBO));

    @Test
    void aimsEveryVineAndOvergrowthRecipeAtEveryAlliedSeedSpiritInReach() {
        BotWorldView world = view()
                .with(object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(3, 0, 0)),
                        object(2, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(5, 0, 0)),
                        object(3, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(12, 0, 0)),
                        object(4, ENEMY_SIDE, PrefabType.SeedSpirit, SELF.plus(4, 0, 0)))
                .build();

        List<RuleOutcome> outcomes = engine.evaluate(world, List.of(VINE_TOSS, OVERGROWTH, FIRE_SHOT), 0);

        assertThat(outcomes).extracting(RuleOutcome::ruleId).containsOnly("combo.seed-spirit");
        assertThat(outcomes).extracting(RuleOutcome::reason).containsExactlyInAnyOrder(
                "VineTossMagic targets allied SeedSpirit 1.",
                "VineTossMagic targets allied SeedSpirit 2.",
                "OvergrowthMagic targets allied SeedSpirit 1.",
                "OvergrowthMagic targets allied SeedSpirit 2.");
        assertThat(outcomes).extracting(RuleOutcome::target)
                .contains(SELF.plus(3, 0, 0), SELF.plus(5, 0, 0));
        assertThat(outcomes).filteredOn(outcome -> outcome.cost() == 4)
                .allSatisfy(outcome -> assertThat(outcome.score()).isEqualTo(1.0 / 4));
        assertThat(outcomes).filteredOn(outcome -> outcome.cost() == 6)
                .allSatisfy(outcome -> assertThat(outcome.score()).isEqualTo(1.0 / 6));
    }

    @Test
    void putsEveryExplosionRecipeOnTheCentreOfTheMobCluster() {
        Vector3 center = SELF.plus(4, 0, 0);
        BotWorldView world = view()
                .withCluster(cluster(center,
                        object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, 1)),
                        object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, -1)),
                        object(3, ENEMY_SIDE, PrefabType.EmberSpirit, center)))
                .build();

        List<RuleOutcome> outcomes = engine.evaluate(world, List.of(VINE_TOSS, MAGMA, FIRE_SHOT), 0);

        assertThat(outcomes).singleElement().satisfies(outcome -> {
            assertThat(outcome.ruleId()).isEqualTo("combo.mob-cluster");
            assertThat(outcome.target()).isEqualTo(center);
            assertThat(outcome.cost()).isEqualTo(5);
            assertThat(outcome.score()).isEqualTo(3.0 / 5);
            assertThat(outcome.reason()).isEqualTo("MagmaExplosionMagic targets a cluster of 3 enemy mobs.");
        });
    }

    @Test
    void prefersTheSeedSpiritComboOverTheClusterComboWhenBothApply() {
        BotWorldView world = view()
                .with(object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(3, 0, 0)))
                .withCluster(cluster(SELF.plus(4, 0, 0),
                        object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, 1)),
                        object(3, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, -1)),
                        object(4, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, 0))))
                .build();

        assertThat(engine.evaluate(world, List.of(VINE_TOSS, MAGMA), 0))
                .extracting(RuleOutcome::ruleId)
                .containsOnly("combo.seed-spirit");
    }

    @Test
    void saysNothingWhenNeitherTacticApplies() {
        BotWorldView world = view()
                .with(object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, 0)),
                        object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(5, 0, 0)))
                .build();

        assertThat(engine.evaluate(world, List.of(VINE_TOSS, MAGMA, FIRE_SHOT), 0)).isEmpty();
    }

    @Test
    void ignoresAClusterTooSmallOrTooFarAwayJustAsTheHandWrittenTacticDid() {
        BotWorldView tooSmall = view()
                .withCluster(cluster(SELF.plus(4, 0, 0),
                        object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, 1)),
                        object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, -1))))
                .build();
        BotWorldView tooFar = view()
                .withCluster(cluster(SELF.plus(12, 0, 0),
                        object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(12, 0, 1)),
                        object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(12, 0, -1)),
                        object(3, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(12, 0, 0))))
                .build();

        assertThat(engine.evaluate(tooSmall, List.of(MAGMA), 0)).isEmpty();
        assertThat(engine.evaluate(tooFar, List.of(MAGMA), 0)).isEmpty();
    }
}
