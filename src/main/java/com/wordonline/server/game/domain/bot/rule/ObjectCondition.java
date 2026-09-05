package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.bot.view.ObservedObject;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * "Something matching this description is on the field."
 *
 * <p>Matches are ranked nearest-first, so a rule that produces one candidate per subject offers the
 * closest one first and repeated think passes over an unchanged field produce an identical list.
 *
 * @param filter   what the subject looks like
 * @param minCount how many of them there have to be before the clause holds
 */
public record ObjectCondition(TargetFilter filter, int minCount) implements BotCondition {

    @Override
    public Optional<ConditionMatch> match(ConditionContext context) {
        List<ObservedObject> matched = context.view().objects().stream()
                .filter(object -> filter.matches(object, context))
                .sorted(Comparator
                        .comparingDouble((ObservedObject object) -> object.distanceTo(context.selfPosition()))
                        .thenComparingInt(ObservedObject::id))
                .toList();
        if (matched.size() < minCount) {
            return Optional.empty();
        }
        return Optional.of(ConditionMatch.ofObjects(matched));
    }
}
