package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.bot.view.ObservedObject;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

import java.util.Set;

/**
 * Which objects on the field a tactic is talking about.
 *
 * <p>This is the "subject" half of the sentences a rule can say: something of a given kind, on a
 * given side, that is attacking me, or near me, or coming at me. Every predicate delegates to
 * {@link ObservedObject}, which already knows how to answer them; nothing is recomputed here.
 *
 * <p>Built through {@link TargetSelector}, never directly, so a tactic reads as prose.
 *
 * @param side              which side owns the object
 * @param prefabTypes       accepted prefab templates; empty accepts every type
 * @param tags              gameplay tags the object must carry, all of them; empty accepts anything
 * @param attackingOnly     only objects whose status says they are swinging right now
 * @param mobOnly           only objects carrying a mob component, so structures and effects drop out
 * @param targetableOnly    only objects that can still be hit; on by default, because a rule that
 *                          aims at a corpse produces a cast the game will discard
 * @param minDistanceFromBot   nearest distance from the bot that still counts, inclusive
 * @param maxDistanceFromBot   furthest distance from the bot that still counts, inclusive;
 *                             {@link Double#POSITIVE_INFINITY} when the tactic sets no bound
 * @param castRangeGated       additionally cap the distance at the candidate recipe's cast range
 * @param minApproachSpeed     how fast the object must be closing on the bot, in units per second;
 *                             {@link Double#NEGATIVE_INFINITY} when the tactic does not care
 */
public record TargetFilter(
        TargetSide side,
        Set<PrefabType> prefabTypes,
        Set<String> tags,
        boolean attackingOnly,
        boolean mobOnly,
        boolean targetableOnly,
        double minDistanceFromBot,
        double maxDistanceFromBot,
        boolean castRangeGated,
        double minApproachSpeed
) {

    public TargetFilter {
        prefabTypes = Set.copyOf(prefabTypes);
        tags = Set.copyOf(tags);
    }

    /** True when {@code object} is one of the things this filter is talking about. */
    public boolean matches(ObservedObject object, ConditionContext context) {
        if (object == null) {
            return false;
        }
        if (!sideMatches(object, context)) {
            return false;
        }
        if (targetableOnly && !object.targetable()) {
            return false;
        }
        if (mobOnly && !object.mob()) {
            return false;
        }
        if (attackingOnly && !object.isAttacking()) {
            return false;
        }
        if (!prefabTypes.isEmpty() && !prefabTypes.contains(object.type())) {
            return false;
        }
        for (String tag : tags) {
            if (!object.hasTag(tag)) {
                return false;
            }
        }
        double distance = object.distanceTo(context.selfPosition());
        if (distance < minDistanceFromBot || distance > effectiveMaxDistance(context)) {
            return false;
        }
        return object.approachSpeedToward(context.selfPosition()) >= minApproachSpeed;
    }

    /** The distance bound actually applied, once the recipe's reach is taken into account. */
    public double effectiveMaxDistance(ConditionContext context) {
        return castRangeGated
                ? Math.min(maxDistanceFromBot, context.castRange())
                : maxDistanceFromBot;
    }

    private boolean sideMatches(ObservedObject object, ConditionContext context) {
        return switch (side) {
            case ALLY -> object.master() == context.view().botSide();
            case ENEMY -> object.master() == context.view().enemySide();
            case ANY -> true;
        };
    }
}
