package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.bot.domain.BotTier;
import com.wordonline.server.game.domain.magic.implement.explode.AbstractExplosionMagic;
import com.wordonline.server.game.domain.magic.implement.explode.OvergrowthMagic;
import com.wordonline.server.game.domain.magic.implement.shoot.VineTossMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

import java.util.List;

import static com.wordonline.server.game.domain.bot.rule.BotRules.ONE_PER_MANA;
import static com.wordonline.server.game.domain.bot.rule.BotRules.ally;
import static com.wordonline.server.game.domain.bot.rule.BotRules.clusterCenter;
import static com.wordonline.server.game.domain.bot.rule.BotRules.clusterSizePerMana;
import static com.wordonline.server.game.domain.bot.rule.BotRules.enemyCluster;
import static com.wordonline.server.game.domain.bot.rule.BotRules.matchedTarget;
import static com.wordonline.server.game.domain.bot.rule.BotRules.rule;

/**
 * The tactics the bot plays before it falls back to scoring every recipe it can afford.
 *
 * <p>This file is the point of the rule layer: a tactic is a sentence here, not a method on the
 * brain. Adding one means adding a rule; it does not mean finding somewhere in a scoring formula
 * to hide the intent.
 *
 * <p>Order is by {@code priority}, and the first rule that finds a target wins outright - the
 * brain does not mix a rule's cast with the value scorer's. A rule is therefore a claim that this
 * play is worth more than whatever the scorer would have chosen, so keep the list short.
 */
public final class BotRuleBook {

    /** Broadcast to the client on the bot's thought feed, so it is part of the protocol. */
    public static final String SEED_SPIRIT_RULE_ID = "combo.seed-spirit";

    /** Broadcast to the client on the bot's thought feed, so it is part of the protocol. */
    public static final String MOB_CLUSTER_RULE_ID = "combo.mob-cluster";

    /** How far apart enemy mobs may stand and still be worth one area spell. */
    public static final double CLUSTER_RADIUS = 2.5;

    /** How many of them have to be in one place before it is. */
    public static final int CLUSTER_MIN_MOBS = 3;

    /**
     * Every tactic is gated at INTRO or above, which excludes the tutorial bot: {@code HOSPITALITY}
     * sits below {@code INTRO} in {@link BotTier}. That bot is not a weaker opponent but a
     * different job - it plays to lose convincingly - and combat combos are the opposite of it.
     */
    private static final BotTier COMBAT_TIER = BotTier.INTRO;

    private BotRuleBook() {
        // Utility class - prevent instantiation
    }

    public static List<BotRule> defaultRules() {
        return List.of(seedSpiritCombo(), mobClusterCombo());
    }

    /**
     * Grow the bot's own seed spirit. The target is an ALLIED unit, which is the case a rule layer
     * has to support and a target-the-nearest-enemy heuristic never reaches: the spell is aimed at
     * something the bot owns, to make it into something bigger.
     */
    private static BotRule seedSpiritCombo() {
        return rule(SEED_SPIRIT_RULE_ID)
                .priority(100)
                .tierAtLeast(COMBAT_TIER)
                .castingMagic(VineTossMagic.class, OvergrowthMagic.class)
                .when(ally(PrefabType.SeedSpirit).inCastRange())
                .at(matchedTarget())
                .scoring(ONE_PER_MANA);
    }

    /**
     * Drop an explosion on a crowd. Scored by how many bodies the crowd holds per mana spent, so a
     * cheap spell on a big group beats an expensive one on a barely-qualifying group.
     */
    private static BotRule mobClusterCombo() {
        return rule(MOB_CLUSTER_RULE_ID)
                .priority(90)
                .tierAtLeast(COMBAT_TIER)
                .castingMagic(AbstractExplosionMagic.class)
                .when(enemyCluster().minSize(CLUSTER_MIN_MOBS).inCastRange())
                .at(clusterCenter())
                .scoring(clusterSizePerMana());
    }
}
