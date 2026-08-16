package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.CardType;

import java.util.List;

/**
 * Card-level gameplay parameters the bot needs to evaluate a recipe before casting it.
 *
 * <p>Values are read per card type rather than per concrete magic, because the recipe is only
 * resolved into a {@code Magic} once the cast reaches {@code MagicInputHandler}.
 */
public final class BotSpellStats {

    /**
     * Charged when a card has no {@code mana_cost} row. Deliberately unaffordable so a recipe with
     * unknown pricing is skipped instead of being cast and rejected by the input handler.
     */
    static final int UNKNOWN_MANA_COST = 9_999;

    private final Parameters parameters;

    public BotSpellStats(Parameters parameters) {
        this.parameters = parameters;
    }

    /**
     * Total mana the recipe costs, summed over every card, matching what the input handler charges.
     *
     * <p>Costing only the magic card made the bot believe every recipe was as cheap as its main
     * card, so it queued casts it could not pay for and the input handler rejected them.
     */
    public int totalManaCost(List<CardType> recipe) {
        int total = 0;
        for (CardType card : recipe) {
            total += (int) parameters.getValueOrDefault(card.name(), "mana_cost", UNKNOWN_MANA_COST);
            if (total >= UNKNOWN_MANA_COST) {
                return UNKNOWN_MANA_COST;
            }
        }
        return total;
    }

    /** Maximum distance from the caster the target position may be. */
    public double castRange(CardType mainCard) {
        return parameters.getValueOrDefault(mainCard.name(), "range", 0.0);
    }
}
