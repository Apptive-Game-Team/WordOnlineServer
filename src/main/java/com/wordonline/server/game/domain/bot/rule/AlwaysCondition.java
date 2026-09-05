package com.wordonline.server.game.domain.bot.rule;

import java.util.Optional;

/**
 * Holds unconditionally, naming no subject. The default "when" of a rule that only cares which
 * spell it is casting and where it wants to put it.
 */
public record AlwaysCondition() implements BotCondition {

    @Override
    public Optional<ConditionMatch> match(ConditionContext context) {
        return Optional.of(ConditionMatch.empty());
    }
}
