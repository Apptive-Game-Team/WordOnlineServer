package com.wordonline.server.game.domain.bot.rule;

import java.util.List;
import java.util.Optional;

/**
 * One clause of a tactic's "when".
 *
 * <p>Matching returns {@link Optional} of {@link ConditionMatch} rather than a boolean on purpose:
 * a condition has to hand the things it found forward, or the action half of the rule has nothing
 * to aim at. See {@link ConditionMatch}.
 *
 * <p>Sealed so the set of clauses a tactic can say stays small and reviewable. Adding a clause is a
 * deliberate act, not something a caller does with a lambda at a call site.
 */
public sealed interface BotCondition
        permits AlwaysCondition, ObjectCondition, ClusterCondition, ManaAtLeastCondition,
                PressureAtLeastCondition, HandContainsCondition, AllCondition, AnyCondition,
                NotCondition {

    /**
     * @param context the frozen world plus the reach of the recipe being considered
     * @return what was found, or empty when the clause does not hold
     */
    Optional<ConditionMatch> match(ConditionContext context);

    /** Both clauses must hold; the subjects of both are carried forward. */
    default BotCondition and(BotCondition other) {
        return new AllCondition(List.of(this, other));
    }

    /** Either clause may hold; the subjects of every clause that held are carried forward. */
    default BotCondition or(BotCondition other) {
        return new AnyCondition(List.of(this, other));
    }

    /** Holds exactly when this clause does not. Names no subjects, so it cannot be aimed at. */
    default BotCondition negate() {
        return new NotCondition(this);
    }
}
