package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.bot.view.BotWorldView;
import com.wordonline.server.game.domain.object.Vector3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Runs a rule book highest priority first and returns the first rule that had anything to say.
 *
 * <p>Nothing here is random. The engine only ranks candidates within one rule using that rule's own
 * scoring function, and hands every one of them back; the per-candidate perturbation that separates
 * an INTRO bot from an ELITE one stays in {@code BotBrain}, applied exactly once per candidate. An
 * engine that drew a random number - to break a tie, to pick a winner, to skip a rule - would move
 * that noise to the wrong side of the seam or apply it twice.
 *
 * <p>Conditions are matched once per candidate recipe rather than once per pass, because "in cast
 * range" is only answerable against a concrete recipe. See {@link ConditionContext}.
 *
 * <h2>State</h2>
 * The only mutable state is the per-rule cooldown clock, and it is a plain {@link HashMap} with no
 * synchronization. That is deliberate: one engine belongs to one {@code BotAgent} and is only ever
 * touched from that agent's own think pass, so there is no second thread to guard against. Sharing
 * one engine between agents would break that assumption, and cooldowns would leak between bots.
 */
public final class PriorityBotRuleEngine implements BotRuleEngine {

    private final List<BotRule> rules;
    private final Map<String, Long> lastProducedAtMillis = new HashMap<>();

    public PriorityBotRuleEngine(List<BotRule> rules) {
        this.rules = rules.stream()
                // A method reference would be ambiguous here: the clause setters overload the
                // accessors of the same name, which is what makes a rule read as one sentence.
                .sorted(Comparator.comparingInt((BotRule rule) -> rule.priority()).reversed())
                .toList();
    }

    /** The tactics this engine runs, highest priority first. */
    public List<BotRule> rules() {
        return rules;
    }

    @Override
    public List<RuleOutcome> evaluate(BotWorldView view, List<RecipeOption> options, long nowMillis) {
        if (view == null || options == null || options.isEmpty()) {
            return List.of();
        }
        for (BotRule rule : rules) {
            if (!rule.allowedFor(view) || onCooldown(rule, nowMillis)) {
                continue;
            }
            List<RuleOutcome> candidates = candidatesOf(rule, view, options);
            if (!candidates.isEmpty()) {
                lastProducedAtMillis.put(rule.id(), nowMillis);
                return candidates;
            }
        }
        return List.of();
    }

    /** Forget every cooldown, for a bot that is starting a fresh match on a reused engine. */
    public void reset() {
        lastProducedAtMillis.clear();
    }

    private boolean onCooldown(BotRule rule, long nowMillis) {
        if (rule.cooldownMillis() <= 0) {
            return false;
        }
        Long last = lastProducedAtMillis.get(rule.id());
        return last != null && nowMillis - last < rule.cooldownMillis();
    }

    private static List<RuleOutcome> candidatesOf(BotRule rule, BotWorldView view, List<RecipeOption> options) {
        List<RuleOutcome> candidates = new ArrayList<>();
        for (RecipeOption option : options) {
            if (!rule.casts(option)) {
                continue;
            }
            ConditionContext context = new ConditionContext(view, option.castRange());
            Optional<ConditionMatch> matched = rule.condition().match(context);
            if (matched.isEmpty()) {
                continue;
            }
            ConditionMatch match = matched.get();
            for (Aim aim : rule.aim().resolve(context, match)) {
                if (!withinReach(aim.position(), view.selfPosition(), option.castRange())) {
                    continue;
                }
                candidates.add(new RuleOutcome(
                        option.recipe(),
                        aim.position(),
                        option.cost(),
                        rule.scoring().score(new ScoringContext(view, option, match, aim)),
                        rule.id(),
                        option.magicName() + " targets " + aim.description() + "."));
            }
        }
        return List.copyOf(candidates);
    }

    // A rule that names a target the recipe cannot reach produces no candidate at all, rather than a
    // cast the input handler would refuse. The bot then falls through to the next rule, which is the
    // behaviour a tactic author expects from "when a seed spirit is in cast range".
    private static boolean withinReach(Vector3 target, Vector3 selfPosition, double castRange) {
        return target.distance(selfPosition) <= castRange;
    }
}
