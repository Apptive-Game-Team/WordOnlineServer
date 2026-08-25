package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.magic.CardType;

import java.util.ArrayList;
import java.util.List;

/**
 * Whether a hand can actually pay out a recipe.
 *
 * <p>Extracted from the scoring path so the rule engine checks feasibility exactly the same way.
 * A rule that fires on a recipe the hand cannot make would be silently dropped by
 * {@code MagicInputHandler} instead, which looks like the bot doing nothing.
 */
public final class RecipeMatcher {

    private RecipeMatcher() {
    }

    /** True when {@code hand} contains every card of {@code recipe}, counting duplicates. */
    public static boolean canMake(List<CardType> hand, List<CardType> recipe) {
        List<CardType> remaining = new ArrayList<>(hand);
        for (CardType card : recipe) {
            int index = remaining.indexOf(card);
            if (index == -1) {
                return false;
            }
            remaining.remove(index);
        }
        return true;
    }
}
