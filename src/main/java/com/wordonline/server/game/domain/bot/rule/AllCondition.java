package com.wordonline.server.game.domain.bot.rule;

import java.util.List;
import java.util.Optional;

/**
 * Every clause has to hold.
 *
 * <p>The subjects of all of them are carried forward, in clause order. Two clauses that each name
 * objects therefore widen what the rule may aim at rather than narrowing it; a tactic that wants
 * "an enemy that is both attacking and close" says so with one {@link TargetSelector}, which is
 * what the selector's chained clauses are for.
 */
public record AllCondition(List<BotCondition> conditions) implements BotCondition {

    public AllCondition {
        conditions = List.copyOf(conditions);
    }

    @Override
    public Optional<ConditionMatch> match(ConditionContext context) {
        ConditionMatch merged = ConditionMatch.empty();
        for (BotCondition condition : conditions) {
            Optional<ConditionMatch> matched = condition.match(context);
            if (matched.isEmpty()) {
                return Optional.empty();
            }
            merged = merged.merge(matched.get());
        }
        return Optional.of(merged);
    }
}
