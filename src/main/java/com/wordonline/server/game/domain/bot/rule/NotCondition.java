package com.wordonline.server.game.domain.bot.rule;

import java.util.Optional;

/**
 * Holds exactly when the wrapped clause does not.
 *
 * <p>Names no subject, deliberately: the things a negated clause found are the things the tactic
 * said it did not want, so aiming at them would be backwards. A rule built only from negations has
 * nothing to aim at and should use an aim that does not need one, such as
 * {@link BotRules#selfPosition()}.
 */
public record NotCondition(BotCondition condition) implements BotCondition {

    @Override
    public Optional<ConditionMatch> match(ConditionContext context) {
        return condition.match(context).isPresent()
                ? Optional.empty()
                : Optional.of(ConditionMatch.empty());
    }
}
