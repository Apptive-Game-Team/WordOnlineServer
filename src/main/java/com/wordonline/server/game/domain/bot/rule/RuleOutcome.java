package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.object.Vector3;

import java.util.List;

/**
 * A cast one rule is asking for, before tier noise is applied.
 *
 * <p>Deliberately not a finished decision: {@code BotBrain} owns the per-candidate random
 * perturbation that separates an INTRO bot from an ELITE one, and it must stay applied exactly
 * once per scored candidate. A rule that produced a finished decision would either skip that or
 * double it.
 *
 * @param recipe the cards to play
 * @param target where the spell lands
 * @param cost   mana charged, carried through so the brain can log and compare consistently
 * @param score  the rule's own value for this cast, already divided by cost where that applies
 * @param ruleId stable identifier of the rule that produced it, surfaced on the decision
 * @param reason human-readable explanation, surfaced on the decision for debugging and replay
 */
public record RuleOutcome(
        List<CardType> recipe,
        Vector3 target,
        int cost,
        double score,
        String ruleId,
        String reason
) {
}
