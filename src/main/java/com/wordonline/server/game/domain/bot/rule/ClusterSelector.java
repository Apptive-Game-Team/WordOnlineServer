package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

/**
 * Builds the sentence "three or more of them bunched up, with the middle of the bunch inside my
 * reach".
 *
 * <p>The grouping itself is not done here: {@link com.wordonline.server.game.domain.bot.view.BotWorldView#enemyClusters()}
 * arrives already grouped, so a tactic only has to say which of those groups are worth a spell.
 *
 * <p>Start one with {@link BotRules#enemyCluster()}.
 */
public final class ClusterSelector {

    private int minSize = 1;
    private int minTotalHp;
    private TargetSelector memberSelector;

    ClusterSelector() {
    }

    /** How many bodies have to be in the group. */
    public ClusterSelector minSize(int size) {
        minSize = Math.max(1, size);
        return this;
    }

    /**
     * Skip groups whose members are collectively about to die anyway, so a big spell is not spent
     * on chaff that the bot's existing board would have finished.
     */
    public ClusterSelector minTotalHp(int totalHp) {
        minTotalHp = totalHp;
        return this;
    }

    /**
     * Count only members that match, so a tactic can say "three skeletons bunched up" rather than
     * "three of anything". The aim point stays the whole group's centre, which is what
     * {@link com.wordonline.server.game.domain.bot.view.ObjectCluster} computed it for.
     */
    public ClusterSelector of(PrefabType... types) {
        memberSelector = BotRules.anyUnit(types);
        return this;
    }

    /** Count only members matching a full selector, for the cases {@link #of} cannot express. */
    public ClusterSelector membersMatching(TargetSelector selector) {
        memberSelector = selector;
        return this;
    }

    /** Finish: the group's centre must be within the reach of whichever recipe the rule is casting. */
    public BotCondition inCastRange() {
        return build(Double.POSITIVE_INFINITY, true);
    }

    /** Finish: the group's centre must be within a fixed distance of the bot. */
    public BotCondition within(double distance) {
        return build(distance, false);
    }

    /** Finish: how far away the group sits does not matter. */
    public BotCondition anywhere() {
        return build(Double.POSITIVE_INFINITY, false);
    }

    private BotCondition build(double maxCenterDistance, boolean castRangeGated) {
        TargetFilter memberFilter = memberSelector == null ? null : memberSelector.unboundedFilter();
        return new ClusterCondition(minSize, minTotalHp, memberFilter, maxCenterDistance, castRangeGated);
    }
}
