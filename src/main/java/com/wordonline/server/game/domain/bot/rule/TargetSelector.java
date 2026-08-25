package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Builds the sentence "an enemy skeleton that is attacking, within my reach" one clause at a time.
 *
 * <p>Every clause returns the selector again, so they chain. The clauses that fix how far away the
 * subject may be finish the sentence and hand back a {@link BotCondition}: a tactic must say what
 * it means by "near me" before the condition is usable, and cast range is the answer often enough
 * that {@link #inCastRange()} is the common ending.
 *
 * <p>Start one with {@link BotRules#ally}, {@link BotRules#enemy} or {@link BotRules#anyUnit}.
 */
public final class TargetSelector {

    private final TargetSide side;
    private final Set<PrefabType> prefabTypes = new LinkedHashSet<>();
    private final Set<String> tags = new LinkedHashSet<>();
    private boolean attackingOnly;
    private boolean mobOnly;
    private boolean targetableOnly = true;
    private double minDistanceFromBot;
    private double minApproachSpeed = Double.NEGATIVE_INFINITY;
    private int minCount = 1;

    TargetSelector(TargetSide side, PrefabType... prefabTypes) {
        this.side = side;
        this.prefabTypes.addAll(Arrays.asList(prefabTypes));
    }

    /** Also accept these prefab templates. */
    public TargetSelector ofType(PrefabType... types) {
        prefabTypes.addAll(Arrays.asList(types));
        return this;
    }

    /** Require every one of these gameplay tags, the same vocabulary the counter rules use. */
    public TargetSelector withTag(String... requiredTags) {
        tags.addAll(Arrays.asList(requiredTags));
        return this;
    }

    /** Only objects that are swinging right now. */
    public TargetSelector attacking() {
        attackingOnly = true;
        return this;
    }

    /** Only objects that are bodies on the field, so structures and spell effects drop out. */
    public TargetSelector mobs() {
        mobOnly = true;
        return this;
    }

    /** Count objects that can no longer be hit as well; off by default. */
    public TargetSelector includeUntargetable() {
        targetableOnly = false;
        return this;
    }

    /** Ignore anything closer than this, for a tactic that wants the far half of the field. */
    public TargetSelector beyond(double distance) {
        minDistanceFromBot = distance;
        return this;
    }

    /**
     * Only objects closing on the bot at least this fast, in units per second. This is the "coming
     * at me" clause: something running past the bot barely registers, something running straight at
     * it scores its full speed.
     */
    public TargetSelector approachingAtLeast(double unitsPerSecond) {
        minApproachSpeed = unitsPerSecond;
        return this;
    }

    /** Require at least this many matching objects before the condition counts as met. */
    public TargetSelector atLeast(int count) {
        minCount = Math.max(1, count);
        return this;
    }

    /** Finish: the subject must be within the reach of whichever recipe the rule is casting. */
    public BotCondition inCastRange() {
        return new ObjectCondition(filter(Double.POSITIVE_INFINITY, true), minCount);
    }

    /** Finish: the subject must be within a fixed distance of the bot. */
    public BotCondition within(double distance) {
        return new ObjectCondition(filter(distance, false), minCount);
    }

    /** Finish: distance from the bot does not matter. Aim resolution still refuses illegal casts. */
    public BotCondition anywhere() {
        return new ObjectCondition(filter(Double.POSITIVE_INFINITY, false), minCount);
    }

    /** The clauses so far with no distance bound, for use as a cluster-membership test. */
    TargetFilter unboundedFilter() {
        return filter(Double.POSITIVE_INFINITY, false);
    }

    private TargetFilter filter(double maxDistance, boolean castRangeGated) {
        return new TargetFilter(
                side,
                prefabTypes,
                tags,
                attackingOnly,
                mobOnly,
                targetableOnly,
                minDistanceFromBot,
                maxDistance,
                castRangeGated,
                minApproachSpeed);
    }
}
