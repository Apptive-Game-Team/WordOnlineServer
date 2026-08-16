package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.CardType;

import java.util.List;

/**
 * Card-level gameplay parameters the bot needs to evaluate a recipe before casting it.
 *
 * <p>Values are read per card type rather than per concrete magic, because the recipe is only
 * resolved into a {@code Magic} once the cast reaches {@code MagicInputHandler}. Damage and radius
 * are therefore an approximation of the spell that will actually spawn; mana cost is exact, since
 * {@code PlayerData.useCards} charges the cost of every card in the recipe.
 */
public final class BotSpellStats {

    /**
     * Charged when a card has no {@code mana_cost} row. Deliberately unaffordable so a recipe with
     * unknown pricing is skipped instead of being cast and rejected by the input handler.
     */
    static final int UNKNOWN_MANA_COST = 9_999;

    /** Used when a magic card has no {@code damage} row, so blast scoring still ranks by coverage. */
    static final double UNKNOWN_DAMAGE = 8.0;

    /** Used when a magic card has no {@code radius} row (for example {@code spawn}). */
    static final double UNKNOWN_BLAST_RADIUS = 0.5;

    private final Parameters parameters;

    public BotSpellStats(Parameters parameters) {
        this.parameters = parameters;
    }

    /**
     * Total mana the recipe costs, summed over every card, matching what the input handler charges.
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

    /** Radius around the target position that the spell is expected to cover. */
    public double blastRadius(CardType mainCard) {
        return parameters.getValueOrDefault(mainCard.name(), "radius", UNKNOWN_BLAST_RADIUS);
    }

    /** Expected damage applied to each covered target. */
    public double damagePerTarget(CardType mainCard) {
        double damage = parameters.getValueOrDefault(mainCard.name(), "damage", UNKNOWN_DAMAGE);
        return damage > 0 ? damage : UNKNOWN_DAMAGE;
    }
}
