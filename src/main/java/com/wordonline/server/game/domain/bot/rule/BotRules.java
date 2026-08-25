package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

import java.util.List;
import java.util.Set;

/**
 * The words a tactic is written in. Static-import this and a rule book reads as a list of sentences:
 *
 * <pre>{@code
 * rule("combo.seed-spirit")
 *         .priority(100)
 *         .castingMagic(VineTossMagic.class, OvergrowthMagic.class)
 *         .when(ally(PrefabType.SeedSpirit).inCastRange())
 *         .at(matchedTarget())
 *         .scoring(ONE_PER_MANA)
 * }</pre>
 *
 * <p>Java rather than data on purpose: a prefab that is renamed, a magic class that is deleted or a
 * scoring function that no longer compiles should break the build, not the bot at runtime in front
 * of a player.
 */
public final class BotRules {

    /** One point for the cast, divided by what it costs. What the seed-spirit combo has always used. */
    public static final RuleScoring ONE_PER_MANA = context -> 1.0 / context.cost();

    private BotRules() {
    }

    // Rules

    /** Start a tactic. The id is what appears on the decision and in the logs. */
    public static BotRule rule(String id) {
        return new BotRule(
                id,
                0,
                null,
                0,
                Set.of(),
                new AlwaysCondition(),
                new AimSpec.MatchedTarget(),
                ONE_PER_MANA);
    }

    // Subjects

    /** The bot's own units, optionally narrowed to given prefab templates. */
    public static TargetSelector ally(PrefabType... types) {
        return new TargetSelector(TargetSide.ALLY, types);
    }

    /** The opponent's units, optionally narrowed to given prefab templates. */
    public static TargetSelector enemy(PrefabType... types) {
        return new TargetSelector(TargetSide.ENEMY, types);
    }

    /** Units on either side, optionally narrowed to given prefab templates. */
    public static TargetSelector anyUnit(PrefabType... types) {
        return new TargetSelector(TargetSide.ANY, types);
    }

    /** A group of enemy bodies standing close enough together for one area spell. */
    public static ClusterSelector enemyCluster() {
        return new ClusterSelector();
    }

    // Conditions about the bot itself

    public static BotCondition manaAtLeast(int mana) {
        return new ManaAtLeastCondition(mana);
    }

    /** How hard the bot is being pushed, 0 to 1, from the existing threat model. */
    public static BotCondition pressureAtLeast(double pressure) {
        return new PressureAtLeastCondition(pressure);
    }

    /** These cards are in hand, duplicates counted. */
    public static BotCondition handContains(CardType... cards) {
        return new HandContainsCondition(List.of(cards));
    }

    public static BotCondition always() {
        return new AlwaysCondition();
    }

    public static BotCondition allOf(BotCondition... conditions) {
        return new AllCondition(List.of(conditions));
    }

    public static BotCondition anyOf(BotCondition... conditions) {
        return new AnyCondition(List.of(conditions));
    }

    public static BotCondition not(BotCondition condition) {
        return new NotCondition(condition);
    }

    // Aims

    /** At each object the conditions found. */
    public static AimSpec matchedTarget() {
        return new AimSpec.MatchedTarget();
    }

    /** At the middle of each group the conditions found. */
    public static AimSpec clusterCenter() {
        return new AimSpec.ClusterCenter();
    }

    /** On top of the bot. */
    public static AimSpec selfPosition() {
        return new AimSpec.SelfPosition();
    }

    /** A fixed distance in front of the bot, towards the enemy side. */
    public static AimSpec forwardOffset(double distance) {
        return new AimSpec.ForwardOffset(distance);
    }

    // Scoring

    /** One point per body in the group being aimed at, divided by what the cast costs. */
    public static RuleScoring clusterSizePerMana() {
        return context -> (double) context.aimedClusterSize() / context.cost();
    }

    /** The recipe's expected damage on one target, divided by what the cast costs. */
    public static RuleScoring damagePerMana() {
        return context -> context.option().damagePerTarget() / context.cost();
    }

    /** A flat value, for a tactic whose whole point is that it outranks the ones below it. */
    public static RuleScoring constantScore(double value) {
        return context -> value;
    }
}
