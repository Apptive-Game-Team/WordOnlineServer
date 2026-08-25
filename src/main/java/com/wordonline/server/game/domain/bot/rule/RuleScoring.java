package com.wordonline.server.game.domain.bot.rule;

/**
 * How much one rule thinks one candidate cast is worth.
 *
 * <p>Per rule rather than fixed, because the tactics already in the code disagree: the seed-spirit
 * combo values a cast at one over its mana cost, the cluster combo at one per body over its mana
 * cost. A single engine-wide scoring function could not express both, and forcing them to share one
 * would silently re-tune whichever tactic lost the argument.
 *
 * <p>Scores are compared only against other candidates of the same rule, since the engine returns
 * one rule's candidates and stops. A rule is therefore free to use whatever scale suits it.
 */
@FunctionalInterface
public interface RuleScoring {

    double score(ScoringContext context);
}
