package com.wordonline.server.game.domain.bot.rule;

import java.util.Optional;

/**
 * "I have at least this much mana."
 *
 * <p>Affordability of the recipe itself is already settled by the time a rule runs - every
 * {@link RecipeOption} handed to the engine is one the bot can pay for. This clause is for tactics
 * that want a reserve on top, so a cheap combo does not empty the bar right before an answer is
 * needed.
 */
public record ManaAtLeastCondition(int mana) implements BotCondition {

    @Override
    public Optional<ConditionMatch> match(ConditionContext context) {
        return context.view().mana() >= mana ? Optional.of(ConditionMatch.empty()) : Optional.empty();
    }
}
