package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.bot.RecipeMatcher;
import com.wordonline.server.game.domain.magic.CardType;

import java.util.List;
import java.util.Optional;

/**
 * "I am holding these cards", duplicates counted.
 *
 * <p>Delegates to {@link RecipeMatcher} so a tactic reads the hand exactly the way the scoring path
 * and the input handler do. A clause that counted cards its own way would eventually disagree with
 * them, and the disagreement would look like the bot ignoring its own rule.
 */
public record HandContainsCondition(List<CardType> cards) implements BotCondition {

    public HandContainsCondition {
        cards = List.copyOf(cards);
    }

    @Override
    public Optional<ConditionMatch> match(ConditionContext context) {
        return RecipeMatcher.canMake(context.view().hand(), cards)
                ? Optional.of(ConditionMatch.empty())
                : Optional.empty();
    }
}
