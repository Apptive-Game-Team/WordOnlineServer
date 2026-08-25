package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.bot.view.BotWorldView;
import com.wordonline.server.game.domain.object.Vector3;

/**
 * What a condition is matched against: the frozen world, plus the reach of the recipe currently
 * being considered.
 *
 * <p>Cast range belongs here rather than on {@link com.wordonline.server.game.domain.bot.view.BotWorldView}
 * because it is a property of the spell, not of the field. "A seed spirit is in cast range" is only
 * answerable once a concrete {@link RecipeOption} is on the table, so the engine matches a rule's
 * conditions once per candidate recipe rather than once per think pass. Rules and recipes are both
 * counted in single digits, so the repetition costs nothing and it keeps
 * {@link TargetSelector#inCastRange()} an honest filter instead of a comment.
 *
 * @param view      the frozen world for this think pass
 * @param castRange how far from the bot the recipe under consideration can reach
 */
public record ConditionContext(BotWorldView view, double castRange) {

    /** The position the bot casts from and defends; every distance in a condition is measured from it. */
    public Vector3 selfPosition() {
        return view.selfPosition();
    }
}
