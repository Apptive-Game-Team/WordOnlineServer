package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.bot.domain.BotTier;
import com.wordonline.server.game.domain.bot.view.BotWorldView;
import com.wordonline.server.game.domain.magic.implement.explode.AbstractExplosionMagic;
import com.wordonline.server.game.domain.magic.implement.explode.MagmaExplosionMagic;
import com.wordonline.server.game.domain.magic.implement.shoot.FireShotMagic;
import com.wordonline.server.game.domain.magic.implement.shoot.VineTossMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.BOT_SIDE;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.ENEMY_SIDE;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.SELF;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.cluster;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.object;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.option;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.view;
import static com.wordonline.server.game.domain.bot.rule.BotRules.ally;
import static com.wordonline.server.game.domain.bot.rule.BotRules.clusterCenter;
import static com.wordonline.server.game.domain.bot.rule.BotRules.clusterSizePerMana;
import static com.wordonline.server.game.domain.bot.rule.BotRules.enemy;
import static com.wordonline.server.game.domain.bot.rule.BotRules.enemyCluster;
import static com.wordonline.server.game.domain.bot.rule.BotRules.matchedTarget;
import static com.wordonline.server.game.domain.bot.rule.BotRules.rule;
import static org.assertj.core.api.Assertions.assertThat;

class PriorityBotRuleEngineTest {

    private static final RecipeOption VINE_TOSS = option(new VineTossMagic(), 4, 8);
    private static final RecipeOption MAGMA = option(new MagmaExplosionMagic(), 5, 8);

    @Test
    void returnsTheCandidatesOfTheHighestPriorityRuleThatMatchedAndStopsThere() {
        BotWorldView world = view()
                .with(object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(3, 0, 0)),
                        object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, 0)))
                .build();
        PriorityBotRuleEngine engine = new PriorityBotRuleEngine(List.of(
                rule("low").priority(10)
                        .castingMagic(VineTossMagic.class)
                        .when(enemy(PrefabType.RockGolem).inCastRange())
                        .at(matchedTarget()),
                rule("high").priority(90)
                        .castingMagic(VineTossMagic.class)
                        .when(ally(PrefabType.SeedSpirit).inCastRange())
                        .at(matchedTarget())));

        List<RuleOutcome> outcomes = engine.evaluate(world, List.of(VINE_TOSS), 0);

        assertThat(outcomes).extracting(RuleOutcome::ruleId).containsExactly("high");
    }

    @Test
    void fallsThroughToTheNextRuleWhenTheHigherOneMatchesNothing() {
        BotWorldView world = view()
                .with(object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, 0)))
                .build();
        PriorityBotRuleEngine engine = new PriorityBotRuleEngine(List.of(
                rule("low").priority(10)
                        .castingMagic(VineTossMagic.class)
                        .when(enemy(PrefabType.RockGolem).inCastRange())
                        .at(matchedTarget()),
                rule("high").priority(90)
                        .castingMagic(VineTossMagic.class)
                        .when(ally(PrefabType.SeedSpirit).inCastRange())
                        .at(matchedTarget())));

        assertThat(engine.evaluate(world, List.of(VINE_TOSS), 0))
                .extracting(RuleOutcome::ruleId)
                .containsExactly("low");
    }

    @Test
    void producesNothingWhenNoAffordableRecipeCastsTheSpellTheRuleIsAbout() {
        BotWorldView world = view()
                .with(object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(3, 0, 0)))
                .build();
        PriorityBotRuleEngine engine = new PriorityBotRuleEngine(List.of(
                rule("combo").castingMagic(VineTossMagic.class)
                        .when(ally(PrefabType.SeedSpirit).inCastRange())
                        .at(matchedTarget())));

        assertThat(engine.evaluate(world, List.of(option(new FireShotMagic(), 3, 8)), 0)).isEmpty();
    }

    @Test
    void selectsSpellsByAbstractSuperclassSoAWholeFamilyIsNamedAtOnce() {
        BotWorldView world = view()
                .with(object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(3, 0, 0)))
                .build();
        PriorityBotRuleEngine engine = new PriorityBotRuleEngine(List.of(
                rule("boom").castingMagic(AbstractExplosionMagic.class)
                        .when(enemy().inCastRange())
                        .at(matchedTarget())));

        assertThat(engine.evaluate(world, List.of(VINE_TOSS, MAGMA), 0))
                .singleElement()
                .extracting(RuleOutcome::recipe)
                .isEqualTo(MAGMA.recipe());
    }

    @Test
    void producesNoCandidateForATargetTheRecipeCannotReach() {
        BotWorldView world = view()
                .with(object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(12, 0, 0)))
                .build();
        PriorityBotRuleEngine engine = new PriorityBotRuleEngine(List.of(
                rule("combo").castingMagic(VineTossMagic.class)
                        .when(ally(PrefabType.SeedSpirit).anywhere())
                        .at(matchedTarget())));

        assertThat(engine.evaluate(world, List.of(VINE_TOSS), 0)).isEmpty();
        assertThat(engine.evaluate(world, List.of(option(new VineTossMagic(), 4, 20)), 0)).hasSize(1);
    }

    @Test
    void producesOneCandidatePerMatchedTarget() {
        BotWorldView world = view()
                .with(object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(2, 0, 0)),
                        object(2, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(3, 0, 0)),
                        object(3, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(4, 0, 0)))
                .build();
        PriorityBotRuleEngine engine = new PriorityBotRuleEngine(List.of(
                rule("combo").castingMagic(VineTossMagic.class)
                        .when(ally(PrefabType.SeedSpirit).inCastRange())
                        .at(matchedTarget())));

        assertThat(engine.evaluate(world, List.of(VINE_TOSS), 0))
                .hasSize(3)
                .extracting(RuleOutcome::reason)
                .containsExactly(
                        "VineTossMagic targets allied SeedSpirit 1.",
                        "VineTossMagic targets allied SeedSpirit 2.",
                        "VineTossMagic targets allied SeedSpirit 3.");
    }

    @Test
    void producesOneCandidatePerMatchedTargetPerRecipe() {
        BotWorldView world = view()
                .with(object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(2, 0, 0)),
                        object(2, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(3, 0, 0)))
                .build();
        PriorityBotRuleEngine engine = new PriorityBotRuleEngine(List.of(
                rule("combo").castingMagic(VineTossMagic.class, AbstractExplosionMagic.class)
                        .when(ally(PrefabType.SeedSpirit).inCastRange())
                        .at(matchedTarget())));

        assertThat(engine.evaluate(world, List.of(VINE_TOSS, MAGMA), 0)).hasSize(4);
    }

    @Test
    void staysQuietUntilItsCooldownHasElapsed() {
        BotWorldView world = view()
                .with(object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(3, 0, 0)))
                .build();
        PriorityBotRuleEngine engine = new PriorityBotRuleEngine(List.of(
                rule("combo").castingMagic(VineTossMagic.class)
                        .cooldownMillis(3000)
                        .when(ally(PrefabType.SeedSpirit).inCastRange())
                        .at(matchedTarget())));

        assertThat(engine.evaluate(world, List.of(VINE_TOSS), 1000)).hasSize(1);
        assertThat(engine.evaluate(world, List.of(VINE_TOSS), 2000)).isEmpty();
        assertThat(engine.evaluate(world, List.of(VINE_TOSS), 3999)).isEmpty();
        assertThat(engine.evaluate(world, List.of(VINE_TOSS), 4000)).hasSize(1);
    }

    @Test
    void letsALowerRuleThroughWhileTheRuleAboveItIsCoolingDown() {
        BotWorldView world = view()
                .with(object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(3, 0, 0)),
                        object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, 0)))
                .build();
        PriorityBotRuleEngine engine = new PriorityBotRuleEngine(List.of(
                rule("high").priority(90).castingMagic(VineTossMagic.class)
                        .cooldownMillis(5000)
                        .when(ally(PrefabType.SeedSpirit).inCastRange())
                        .at(matchedTarget()),
                rule("low").priority(10).castingMagic(VineTossMagic.class)
                        .when(enemy(PrefabType.RockGolem).inCastRange())
                        .at(matchedTarget())));

        assertThat(engine.evaluate(world, List.of(VINE_TOSS), 0))
                .extracting(RuleOutcome::ruleId).containsExactly("high");
        assertThat(engine.evaluate(world, List.of(VINE_TOSS), 100))
                .extracting(RuleOutcome::ruleId).containsExactly("low");
    }

    @Test
    void forgetsEveryCooldownWhenTheEngineIsReset() {
        BotWorldView world = view()
                .with(object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(3, 0, 0)))
                .build();
        PriorityBotRuleEngine engine = new PriorityBotRuleEngine(List.of(
                rule("combo").castingMagic(VineTossMagic.class)
                        .cooldownMillis(3000)
                        .when(ally(PrefabType.SeedSpirit).inCastRange())
                        .at(matchedTarget())));

        assertThat(engine.evaluate(world, List.of(VINE_TOSS), 0)).hasSize(1);
        engine.reset();

        assertThat(engine.evaluate(world, List.of(VINE_TOSS), 100)).hasSize(1);
    }

    @Test
    void excludesABotBelowTheTierTheTacticIsGatedTo() {
        PriorityBotRuleEngine engine = new PriorityBotRuleEngine(List.of(
                rule("combo").castingMagic(VineTossMagic.class)
                        .tierAtLeast(BotTier.INTERMEDIATE)
                        .when(ally(PrefabType.SeedSpirit).inCastRange())
                        .at(matchedTarget())));

        assertThat(engine.evaluate(gatedWorld(BotTier.BEGINNER), List.of(VINE_TOSS), 0)).isEmpty();
        assertThat(engine.evaluate(gatedWorld(BotTier.HOSPITALITY), List.of(VINE_TOSS), 0)).isEmpty();
        assertThat(engine.evaluate(gatedWorld(BotTier.INTERMEDIATE), List.of(VINE_TOSS), 0)).hasSize(1);
        assertThat(engine.evaluate(gatedWorld(BotTier.ELITE), List.of(VINE_TOSS), 0)).hasSize(1);
    }

    @Test
    void scoresWithTheRuleSOwnFunctionRatherThanAFixedOne() {
        BotWorldView world = view()
                .with(object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(3, 0, 0)))
                .withCluster(cluster(SELF.plus(3, 0, 0),
                        object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(3, 0, 0)),
                        object(3, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(3, 0, 1)),
                        object(4, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(3, 0, -1)),
                        object(5, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(3.5f, 0, 0))))
                .build();
        PriorityBotRuleEngine perMana = new PriorityBotRuleEngine(List.of(
                rule("seed").castingMagic(VineTossMagic.class)
                        .when(ally(PrefabType.SeedSpirit).inCastRange())
                        .at(matchedTarget())));
        PriorityBotRuleEngine perCluster = new PriorityBotRuleEngine(List.of(
                rule("cluster").castingMagic(AbstractExplosionMagic.class)
                        .when(enemyCluster().minSize(3).inCastRange())
                        .at(clusterCenter())
                        .scoring(clusterSizePerMana())));

        assertThat(perMana.evaluate(world, List.of(VINE_TOSS), 0).getFirst().score())
                .isEqualTo(1.0 / 4);
        assertThat(perCluster.evaluate(world, List.of(MAGMA), 0).getFirst().score())
                .isEqualTo(4.0 / 5);
    }

    @Test
    void returnsNothingWhenTheBotHasNoAffordableRecipesAtAll() {
        PriorityBotRuleEngine engine = new PriorityBotRuleEngine(List.of(
                rule("combo").castingMagic(VineTossMagic.class)
                        .when(ally(PrefabType.SeedSpirit).inCastRange())
                        .at(matchedTarget())));

        assertThat(engine.evaluate(gatedWorld(BotTier.ELITE), List.of(), 0)).isEmpty();
    }

    @Test
    void consumesNoRandomnessAtAll() throws Exception {
        BotWorldView world = view()
                .with(object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(2, 0, 0)),
                        object(2, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(3, 0, 0)))
                .build();
        PriorityBotRuleEngine engine = new PriorityBotRuleEngine(List.of(
                rule("combo").castingMagic(VineTossMagic.class)
                        .when(ally(PrefabType.SeedSpirit).inCastRange())
                        .at(matchedTarget())));

        // The perturbation that separates an INTRO bot from an ELITE one belongs to BotBrain and is
        // applied exactly once per candidate there. If the engine drew so much as a tie-break, that
        // count would be wrong; identical inputs producing identical outputs is what proves it does not.
        List<RuleOutcome> first = engine.evaluate(world, List.of(VINE_TOSS), 0);
        for (int pass = 0; pass < 50; pass++) {
            assertThat(engine.evaluate(world, List.of(VINE_TOSS), pass)).isEqualTo(first);
        }

        for (Field field : PriorityBotRuleEngine.class.getDeclaredFields()) {
            assertThat(Random.class.isAssignableFrom(field.getType())).isFalse();
        }
    }

    private static BotWorldView gatedWorld(BotTier tier) {
        return view()
                .tier(tier)
                .with(object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(3, 0, 0)))
                .build();
    }
}
