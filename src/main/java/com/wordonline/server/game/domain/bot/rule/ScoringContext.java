package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.bot.view.BotWorldView;

/**
 * Everything a scoring function is allowed to look at for one candidate cast.
 *
 * @param view   the frozen world for this think pass
 * @param option the recipe this candidate would play
 * @param match  everything the rule's conditions found
 * @param aim    the one place this candidate puts the spell
 */
public record ScoringContext(BotWorldView view, RecipeOption option, ConditionMatch match, Aim aim) {

    /** Mana this candidate costs, never below one, so a score can divide by it safely. */
    public int cost() {
        return Math.max(1, option.cost());
    }

    /** Bodies in the group being aimed at, or one when the aim is not about a group. */
    public int aimedClusterSize() {
        return aim.cluster() == null ? 1 : aim.cluster().size();
    }
}
