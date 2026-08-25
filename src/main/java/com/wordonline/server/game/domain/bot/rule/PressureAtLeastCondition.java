package com.wordonline.server.game.domain.bot.rule;

import java.util.Optional;

/**
 * "I am being pushed at least this hard", on the 0 to 1 scale the existing threat model already
 * produces. Lets a defensive tactic stay quiet until the field actually warrants it.
 */
public record PressureAtLeastCondition(double pressure) implements BotCondition {

    @Override
    public Optional<ConditionMatch> match(ConditionContext context) {
        return context.view().pressure() >= pressure
                ? Optional.of(ConditionMatch.empty())
                : Optional.empty();
    }
}
