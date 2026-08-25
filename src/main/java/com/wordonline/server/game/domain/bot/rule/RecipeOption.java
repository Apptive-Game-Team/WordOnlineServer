package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;

import java.util.List;

/**
 * One recipe the bot could actually cast right now, with the numbers a rule needs to judge it.
 *
 * <p>The "hand can make it, the bot can pay for it, this is its main card" filter used to be
 * repeated at every decision site. Doing it once per think pass means a rule states only its
 * tactic, and no rule can accidentally propose a cast the input handler would reject.
 *
 * @param recipe          the cards to play
 * @param magic           the spell the recipe resolves to, which is what tactics select on
 * @param mainCard        the Magic-type card in the recipe; the parameter rows hang off it
 * @param cost            total mana charged for the whole recipe
 * @param castRange       maximum distance from the caster the target may be
 * @param blastRadius     radius the spell is expected to cover
 * @param damagePerTarget expected damage applied to each covered target
 */
public record RecipeOption(
        List<CardType> recipe,
        Magic magic,
        CardType mainCard,
        int cost,
        double castRange,
        double blastRadius,
        double damagePerTarget
) {

    /** True when the recipe resolves to a spell of the given kind, superclasses included. */
    public boolean isMagic(Class<? extends Magic> magicType) {
        return magic != null && magicType.isInstance(magic);
    }

    public String magicName() {
        return magic == null ? "unknown" : magic.getClass().getSimpleName();
    }
}
