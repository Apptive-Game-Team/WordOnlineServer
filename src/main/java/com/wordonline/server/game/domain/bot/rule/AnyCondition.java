package com.wordonline.server.game.domain.bot.rule;

import java.util.List;
import java.util.Optional;

/** At least one clause has to hold; the subjects of every clause that did are carried forward. */
public record AnyCondition(List<BotCondition> conditions) implements BotCondition {

    public AnyCondition {
        conditions = List.copyOf(conditions);
    }

    @Override
    public Optional<ConditionMatch> match(ConditionContext context) {
        ConditionMatch merged = ConditionMatch.empty();
        boolean any = false;
        for (BotCondition condition : conditions) {
            Optional<ConditionMatch> matched = condition.match(context);
            if (matched.isPresent()) {
                any = true;
                merged = merged.merge(matched.get());
            }
        }
        return any ? Optional.of(merged) : Optional.empty();
    }
}
