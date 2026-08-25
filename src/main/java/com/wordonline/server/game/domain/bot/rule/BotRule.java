package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.bot.domain.BotTier;
import com.wordonline.server.game.domain.bot.view.BotWorldView;
import com.wordonline.server.game.domain.magic.Magic;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * One declared tactic: when to do it, what to cast, where to put it, and what it is worth.
 *
 * <p>Immutable, and every clause returns a new rule, so a tactic is written as one chained sentence
 * with no builder and no terminal call. The clause methods overload the accessors of the same name
 * - {@code priority(100)} sets it, {@code priority()} reads it - which keeps the declaration
 * reading like prose while the record stays a plain carrier of values.
 *
 * <p>Start one with {@link BotRules#rule(String)}. Defaults are the harmless ones: no tier gate, no
 * cooldown, any spell, always true, aim at whatever the conditions found, one point per mana.
 *
 * @param id         stable identifier, surfaced on the decision so a replay can name the tactic
 * @param priority   higher runs first; the engine stops at the first rule that produces candidates
 * @param minTier    lowest bot tier allowed to use this tactic, or {@code null} for all of them
 * @param cooldownMillis how long after producing candidates the rule stays quiet
 * @param magicTypes spell classes this tactic casts, superclasses included; empty means any spell
 * @param condition  what has to be true, and what that leaves to aim at
 * @param aim        where the spell goes
 * @param scoring    what one candidate of this tactic is worth
 */
public record BotRule(
        String id,
        int priority,
        BotTier minTier,
        long cooldownMillis,
        Set<Class<? extends Magic>> magicTypes,
        BotCondition condition,
        AimSpec aim,
        RuleScoring scoring
) {

    public BotRule {
        magicTypes = Set.copyOf(magicTypes);
    }

    /** Higher runs first. */
    public BotRule priority(int priority) {
        return new BotRule(id, priority, minTier, cooldownMillis, magicTypes, condition, aim, scoring);
    }

    /**
     * Restrict the tactic to bots at or above a tier, in {@link BotTier} declaration order. Note
     * that {@code HOSPITALITY} sits below {@code INTRO} in that order, so any gate at all excludes
     * the tutorial opponent - which is usually what a combo tactic wants.
     */
    public BotRule tierAtLeast(BotTier tier) {
        return new BotRule(id, priority, tier, cooldownMillis, magicTypes, condition, aim, scoring);
    }

    /** Stay quiet for this long after producing candidates. */
    public BotRule cooldown(Duration cooldown) {
        return cooldownMillis(cooldown.toMillis());
    }

    /** Stay quiet for this many milliseconds after producing candidates. */
    public BotRule cooldownMillis(long cooldownMillis) {
        return new BotRule(id, priority, minTier, cooldownMillis, magicTypes, condition, aim, scoring);
    }

    /**
     * Which spells this tactic is about, selected by class so an abstract family such as
     * {@code AbstractExplosionMagic} names every explosion at once. A literal card list would have
     * to be revisited every time a spell is added to the family.
     */
    @SafeVarargs
    public final BotRule castingMagic(Class<? extends Magic>... types) {
        Set<Class<? extends Magic>> merged = new LinkedHashSet<>(magicTypes);
        merged.addAll(List.of(types));
        return new BotRule(id, priority, minTier, cooldownMillis, merged, condition, aim, scoring);
    }

    /** What has to be true. Several clauses are combined with and. */
    public BotRule when(BotCondition... conditions) {
        BotCondition combined = conditions.length == 1
                ? conditions[0]
                : new AllCondition(List.of(conditions));
        return new BotRule(id, priority, minTier, cooldownMillis, magicTypes, combined, aim, scoring);
    }

    /** Where the spell goes. */
    public BotRule at(AimSpec aim) {
        return new BotRule(id, priority, minTier, cooldownMillis, magicTypes, condition, aim, scoring);
    }

    /** What one candidate of this tactic is worth. */
    public BotRule scoring(RuleScoring scoring) {
        return new BotRule(id, priority, minTier, cooldownMillis, magicTypes, condition, aim, scoring);
    }

    /** True when this tactic is about the spell {@code option} resolves to. */
    public boolean casts(RecipeOption option) {
        if (magicTypes.isEmpty()) {
            return true;
        }
        for (Class<? extends Magic> type : magicTypes) {
            if (option.isMagic(type)) {
                return true;
            }
        }
        return false;
    }

    /** True when the acting bot is strong enough to be allowed this tactic. */
    public boolean allowedFor(BotWorldView view) {
        if (minTier == null) {
            return true;
        }
        return view.tier() != null && view.tier().ordinal() >= minTier.ordinal();
    }
}
