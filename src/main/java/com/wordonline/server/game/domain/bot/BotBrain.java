package com.wordonline.server.game.domain.bot;

import com.wordonline.server.bot.domain.BotPersona;
import com.wordonline.server.bot.domain.BotTier;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.domain.magic.parser.MagicParser;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.bot.BotCounterEvaluator;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Decides what the bot casts and where.
 *
 * <p>Every affordable recipe becomes a candidate. Offensive recipes are scored by the damage the
 * blast would actually convert at its best landing spot; placement recipes are scored by how much
 * the bot needs a body on the field right now. Both are divided by the recipe's real mana cost, so
 * the choice is value per mana rather than "the biggest combo in hand".
 */
@Slf4j
public class BotBrain {

    public record InputDecision(List<CardType> playCards, Vector3 target) {}

    /** Main cards whose spell lands on a target rather than building the bot's own board. */
    private static final Set<CardType> OFFENSIVE_MAIN_CARDS =
            EnumSet.of(CardType.Shoot, CardType.Explode, CardType.Drop);

    /** Tempo value of removing one enemy from the field, on top of the damage itself. */
    static final double KILL_TEMPO_BONUS = 8.0;

    /** Board value a placement recipe is worth per card spent on it. */
    static final double PLACEMENT_VALUE_PER_CARD = 6.0;

    /** How much full defensive pressure inflates the value of putting a body down. */
    static final double DEFENSE_URGENCY_WEIGHT = 1.5;

    /** Scales the tag-based counter score into the same range as the damage-based scores. */
    static final double COUNTER_WEIGHT = 3.0;

    private final MagicParser magicParser;
    private final BotCounterEvaluator counterEvaluator;
    private final BotPersona persona;

    public BotBrain(MagicParser magicParser, BotCounterEvaluator counterEvaluator, BotPersona persona) {
        this.magicParser = magicParser;
        this.counterEvaluator = counterEvaluator;
        this.persona = persona;
    }

    public InputDecision think(List<GameObject> gameObjectList,
                               List<CardType> cardList,
                               GameLoop loop,
                               int mana,
                               int enemyPlayerHp,
                               Master botSide)
    {
        try {
            log.trace("[Bot {}] Start thinking with cards={}, mana={}", botSide, cardList, mana);
            if (cardList.isEmpty()) {
                return null;
            }
            if (!(magicParser instanceof DatabaseMagicParser dbParser)) {
                log.warn("[Bot {}] magicParser is not DatabaseMagicParser, fallback disabled", botSide);
                return null;
            }

            Vector3 playerPos = BotSideUtil.getPlayerPosition(botSide);
            Master enemySide = BotSideUtil.getEnemySide(botSide);
            List<GameObject> enemies = gameObjectList.stream()
                    .filter(go -> go.getMaster() == enemySide)
                    .toList();

            BotSpellStats spellStats = new BotSpellStats(loop.parameters);
            ThreatAssessment threats = ThreatAssessment.observe(gameObjectList, enemySide, playerPos, enemyPlayerHp);
            Random random = ThreadLocalRandom.current();

            Collection<List<CardType>> allRecipes = dbParser.getAllMagicRecipes();
            List<ScoredPlay> plays = new ArrayList<>();
            boolean hasMakeableRecipe = false;
            boolean hasAffordableRecipe = false;

            for (List<CardType> recipe : allRecipes) {
                if (!canMakeRecipe(cardList, recipe)) {
                    continue;
                }
                CardType mainCard = findMainCard(recipe);
                if (mainCard == null) {
                    continue;
                }

                hasMakeableRecipe = true;
                int cost = spellStats.totalManaCost(recipe);
                if (cost > mana) {
                    continue;
                }
                hasAffordableRecipe = true;

                buildPlay(recipe, mainCard, cost, spellStats, threats, enemies, playerPos, botSide, random)
                        .ifPresent(plays::add);
            }

            if (!plays.isEmpty()) {
                ScoredPlay chosen = choosePlay(plays, random);
                log.debug("[Bot {}] Chose {} at {} (score={}, cost={}, pressure={})",
                        botSide, chosen.recipe(), chosen.target(), chosen.score(), chosen.cost(), threats.pressure());
                return new InputDecision(chosen.recipe(), chosen.target());
            }

            if (hasMakeableRecipe && !hasAffordableRecipe) {
                log.debug("[Bot {}] Waiting for mana; makeable recipes exist but none are affordable. mana={}", botSide, mana);
                return null;
            }

            CardType cycleCard = pickCycleCard(cardList, allRecipes, spellStats, mana, random);
            if (cycleCard != null) {
                Vector3 target = PlacementPlanner.plan(
                        playerPos, spellStats.castRange(cycleCard), botSide, threats, random);
                log.debug("[Bot {}] Chose to cycle card: {} at {}", botSide, cycleCard, target);
                return new InputDecision(List.of(cycleCard), target);
            }

            log.trace("[Bot {}] No valid actions found this tick", botSide);
        } catch (Exception e) {
            log.error("[Bot {}] Bot think error", botSide, e);
        }
        return null;
    }

    private Optional<ScoredPlay> buildPlay(List<CardType> recipe,
                                           CardType mainCard,
                                           int cost,
                                           BotSpellStats spellStats,
                                           ThreatAssessment threats,
                                           List<GameObject> enemies,
                                           Vector3 playerPos,
                                           Master botSide,
                                           Random random) {
        double counterValue = counterEvaluator.evaluate(recipe, enemies)
                * persona.normalizedCounterAggression()
                * COUNTER_WEIGHT;
        double castRange = spellStats.castRange(mainCard);

        if (OFFENSIVE_MAIN_CARDS.contains(mainCard)) {
            return BlastTargetSelector.bestImpact(
                            threats.threats(),
                            playerPos,
                            castRange,
                            spellStats.blastRadius(mainCard),
                            spellStats.damagePerTarget(mainCard))
                    .map(impact -> {
                        double value = impact.expectedDamage()
                                + KILL_TEMPO_BONUS * impact.lethalCount()
                                + counterValue;
                        return new ScoredPlay(recipe, impact.center(), cost, value / cost);
                    });
        }

        double value = PLACEMENT_VALUE_PER_CARD * recipe.size()
                * (1 + DEFENSE_URGENCY_WEIGHT * threats.pressure())
                + counterValue;
        Vector3 target = PlacementPlanner.plan(playerPos, castRange, botSide, threats, random);
        return Optional.of(new ScoredPlay(recipe, target, cost, value / cost));
    }

    /**
     * Lower tiers deliberately throw away the ranking part of the time, which is what separates an
     * INTRO opponent from an ELITE one; the earlier flat per-tier score bonus could not, because it
     * applied equally to every candidate and so never changed the pick.
     */
    private ScoredPlay choosePlay(List<ScoredPlay> plays, Random random) {
        if (random.nextDouble() < explorationRate(persona.tier())) {
            return plays.get(random.nextInt(plays.size()));
        }
        return plays.stream()
                .max((a, b) -> Double.compare(a.score(), b.score()))
                .orElse(plays.getFirst());
    }

    static double explorationRate(BotTier tier) {
        return switch (tier) {
            case INTRO -> 0.8;
            case BEGINNER -> 0.5;
            case INTERMEDIATE -> 0.3;
            case ADVANCED -> 0.15;
            case ELITE -> 0.0;
        };
    }

    private static boolean canMakeRecipe(List<CardType> hand, List<CardType> recipe) {
        List<CardType> temp = new ArrayList<>(hand);
        for (CardType c : recipe) {
            int idx = temp.indexOf(c);
            if (idx == -1) {
                return false;
            }
            temp.remove(idx);
        }
        return true;
    }

    private static CardType findMainCard(List<CardType> combo) {
        for (CardType c : combo) {
            if (c.getType() == CardType.Type.Magic) {
                return c;
            }
        }
        return combo.isEmpty() ? null : combo.getFirst();
    }

    /**
     * Nothing in hand combines, so one card is spent to draw a replacement. The card that appears in
     * the fewest recipes is the one least likely to be missed.
     */
    private static CardType pickCycleCard(List<CardType> cardList,
                                          Collection<List<CardType>> allRecipes,
                                          BotSpellStats spellStats,
                                          int mana,
                                          Random random) {
        CardType leastUseful = null;
        int fewestRecipes = Integer.MAX_VALUE;
        for (CardType card : cardList) {
            if (spellStats.totalManaCost(List.of(card)) > mana) {
                continue;
            }
            int usage = (int) allRecipes.stream().filter(recipe -> recipe.contains(card)).count();
            if (usage < fewestRecipes || (usage == fewestRecipes && random.nextBoolean())) {
                fewestRecipes = usage;
                leastUseful = card;
            }
        }
        return leastUseful;
    }

    private record ScoredPlay(List<CardType> recipe, Vector3 target, int cost, double score) {}
}
