package com.wordonline.server.game.domain.bot.rule;

/**
 * Which side of the field a tactic is looking at.
 *
 * <p>{@code ALLY} exists because a rule may legitimately want the bot's own units: the seed-spirit
 * combo aims a spell at an allied {@code SeedSpirit} to make it bloom. A vocabulary that only knew
 * how to name enemies could not say that.
 */
public enum TargetSide {

    /** Objects owned by the acting bot. */
    ALLY,

    /** Objects owned by the opposing side. */
    ENEMY,

    /** Objects on either side, neutral ones included. */
    ANY
}
