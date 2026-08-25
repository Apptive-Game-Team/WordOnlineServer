package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.bot.view.BotWorldView;

import java.util.List;

/**
 * Turns the declared tactics into candidate casts.
 *
 * <p>Returning nothing is the normal case and is not a failure: {@code BotBrain} falls through to
 * the value scorer, which is what keeps a bot with an empty rule book behaving exactly as it did
 * before rules existed.
 *
 * <p>The engine returns every candidate of the <em>single highest-priority rule that produced
 * any</em>, and stops there. It deliberately does not pick the winner among them: the brain owns
 * the per-candidate random perturbation that separates an INTRO bot from an ELITE one, and that
 * has to be applied exactly once to each candidate. An engine that picked a winner would move
 * that noise to the wrong side of the seam, or drop it.
 */
public interface BotRuleEngine {

    /**
     * @param view      the frozen world for this think pass
     * @param options   recipes the bot can make and afford right now
     * @param nowMillis current time, for per-rule cooldowns
     * @return every candidate of the highest-priority matching rule, or empty when none matched
     */
    List<RuleOutcome> evaluate(BotWorldView view, List<RecipeOption> options, long nowMillis);

    /** An engine with no tactics. Used to prove that wiring the engine in changes no behaviour. */
    static BotRuleEngine empty() {
        return (view, options, nowMillis) -> List.of();
    }
}
