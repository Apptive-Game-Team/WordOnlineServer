package com.wordonline.server.game.domain.bot;

import com.wordonline.server.bot.domain.BotPersona;
import com.wordonline.server.bot.domain.BotTier;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.parser.MagicParser;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
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

    /** Smallest recipe the hospitality bot may cast. */
    static final int HOSPITALITY_MIN_CARDS = 2;

    /**
     * Largest recipe the hospitality bot may cast. "Low-grade summon" is a card count: the bot's
     * deck can only assemble summons, but a deck constrains which cards are drawn, not how many of
     * them end up in one recipe, so the ceiling has to live here.
     */
    static final int HOSPITALITY_MAX_CARDS = 3;

    private final MagicParser magicParser;
    private final BotCounterEvaluator counterEvaluator;
    private final BotPersona persona;

    /** Non-null only for the tutorial opponent; every other tier scores plays the ordinary way. */
    private final HospitalityDirector hospitalityDirector;

    public BotBrain(MagicParser magicParser, BotCounterEvaluator counterEvaluator, BotPersona persona) {
        this.magicParser = magicParser;
        this.counterEvaluator = counterEvaluator;
        this.persona = persona;
        this.hospitalityDirector = persona.tier() == BotTier.HOSPITALITY ? new HospitalityDirector() : null;
    }

    // Runs on the bot executor thread. Everything it reads about the world comes from the snapshot,
    // never from a live GameObject; parameters are loaded once per session and read-only after that.
    public InputDecision think(BotEye botEye, Parameters parameters, Master botSide) {
        return think(botEye, parameters, botSide, false);
    }

    /**
     * @param overdue the bot has been silent long enough that standing still would read as going
     *                easy on the player, so it must not hold out for a better moment
     */
    public InputDecision think(BotEye botEye, Parameters parameters, Master botSide, boolean overdue)
    {
        List<CardType> cardList = botEye.cardList();
        int mana = botEye.mana();
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
            List<BotVisibleObject> enemies = botEye.gameObjectList().stream()
                    .filter(go -> go.master() == enemySide)
                    .toList();

            BotSpellStats spellStats = new BotSpellStats(parameters);
            ThreatAssessment threats = ThreatAssessment.observe(
                    botEye.gameObjectList(), enemySide, playerPos, botEye.enemyPlayerHp());
            Random random = ThreadLocalRandom.current();

            BoardValue boardValue = new BoardValue(dbParser, spellStats);
            double aggression = aggression(botEye, threats, botSide);

            // 접대 봇은 플레이어 필드에 서 있는 것보다 싼 소환만 낸다. 마나로 재는 이유는
            // BoardValue에 적어 두었다. 필드가 비어 있으면 상한이 없으므로, 아래에서 가장 싼
            // 소환 하나로 떨어뜨린다.
            int enemyBoardMana = hospitalityDirector == null
                    ? Integer.MAX_VALUE
                    : boardValue.manaOnField(botEye.gameObjectList(), enemySide);

            Collection<List<CardType>> allRecipes = dbParser.getAllMagicRecipes();
            List<ScoredPlay> plays = new ArrayList<>();
            boolean hasMakeableRecipe = false;
            boolean hasAffordableRecipe = false;
            List<CardType> cheapestSummon = null;
            int cheapestSummonCost = Integer.MAX_VALUE;

            for (List<CardType> recipe : allRecipes) {
                if (!canMakeRecipe(cardList, recipe)) {
                    continue;
                }
                CardType mainCard = findMainCard(recipe);
                if (mainCard == null) {
                    continue;
                }
                if (hospitalityDirector != null && !isLowGradeSummon(recipe, mainCard)) {
                    continue;
                }

                hasMakeableRecipe = true;
                int cost = spellStats.totalManaCost(recipe);
                if (cost > mana) {
                    continue;
                }
                hasAffordableRecipe = true;

                if (hospitalityDirector != null && (cheapestSummon == null || cost < cheapestSummonCost)) {
                    cheapestSummon = recipe;
                    cheapestSummonCost = cost;
                }

                // 플레이어 필드보다 싸야 한다. 같으면 안 되고 적어야 한다.
                if (cost >= enemyBoardMana) {
                    continue;
                }

                buildPlay(recipe, mainCard, cost, aggression, spellStats, threats, enemies, playerPos, botSide, random)
                        .ifPresent(plays::add);
            }

            if (!plays.isEmpty()) {
                ScoredPlay chosen = choosePlay(plays, random);
                log.debug("[Bot {}] Chose {} at {} (score={}, cost={}, pressure={})",
                        botSide, chosen.recipe(), chosen.target(), chosen.score(), chosen.cost(), threats.pressure());
                return new InputDecision(chosen.recipe(), chosen.target());
            }

            // 필드보다 싼 소환이 하나도 없다. 그렇다고 가만히 있으면 봐주는 걸로 읽히므로,
            // 낼 수 있는 것 중 가장 싼 소환을 낸다. 규칙을 지킬 수 없을 때 고르는 차선이다.
            if (plays.isEmpty() && cheapestSummon != null && overdue) {
                CardType mainCard = findMainCard(cheapestSummon);
                Vector3 target = PlacementPlanner.plan(
                        playerPos, spellStats.castRange(mainCard), botSide, threats, random);
                log.debug("[Bot {}] Nothing cheaper than the enemy board ({} mana); falling back to {}",
                        botSide, enemyBoardMana, cheapestSummon);
                return new InputDecision(cheapestSummon, target);
            }

            // Holding for mana is the right play for a bot that is trying to win. For one that has
            // been quiet too long it is the wrong one: the player reads the pause, not the reason
            // for it. Overdue, the bot spends a card to cycle toward something it can afford.
            if (hasMakeableRecipe && !hasAffordableRecipe && !overdue) {
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
                                           double aggression,
                                           BotSpellStats spellStats,
                                           ThreatAssessment threats,
                                           List<BotVisibleObject> enemies,
                                           Vector3 playerPos,
                                           Master botSide,
                                           Random random) {
        double counterValue = counterValue(recipe, enemies, aggression);
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
     * The counter term, in whichever direction this persona's aggression asks for.
     *
     * <p>A positive aggression scores how much the recipe beats the enemy board, which is what
     * every ordinary bot wants. A negative one scores how much the enemy board beats the recipe
     * and adds it with the same sign, so the play the enemy answers best ranks highest. That is
     * the hospitality bot: it keeps committing real units, and the units it commits lose to what
     * is already on the field. Simply ranking low on the attacking direction would not do it -
     * "does not beat them" is satisfied by any irrelevant play, including standing still.
     */
    private double counterValue(List<CardType> recipe, List<BotVisibleObject> enemies, double aggression) {
        if (aggression == 0.0) {
            return 0.0;
        }
        double matchup = aggression > 0.0
                ? counterEvaluator.evaluate(recipe, enemies)
                : counterEvaluator.evaluateVulnerability(recipe, enemies);
        return matchup * Math.abs(aggression) * COUNTER_WEIGHT;
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

    /**
     * The counter aggression to score this tick with. Ordinary personas use the value stored on
     * them; the hospitality bot has its value chosen per cast from the state of the board, which is
     * how "lose to what is already summoned" is expressed without ever skipping a cast.
     */
    private double aggression(BotEye botEye, ThreatAssessment threats, Master botSide) {
        if (hospitalityDirector == null) {
            return persona.normalizedCounterAggression();
        }
        Master enemySide = BotSideUtil.getEnemySide(botSide);
        int playerUnits = countUnits(botEye, enemySide);
        int botUnits = countUnits(botEye, botSide);
        double aggression = hospitalityDirector.aggression(playerUnits, botUnits, threats.pressure());
        log.debug("[Bot {}] Hospitality aggression={} (player units={}, own units={}, pressure={})",
                botSide, aggression, playerUnits, botUnits, threats.pressure());
        return aggression;
    }

    /** Bodies on the field for one side. The player core is not a body anyone summoned. */
    private static int countUnits(BotEye botEye, Master side) {
        int count = 0;
        for (BotVisibleObject object : botEye.gameObjectList()) {
            if (object.master() == side && object.type() != PrefabType.Player && object.targetable()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Whether the hospitality bot is allowed to cast this recipe: a summon, of the size a new player
     * can be expected to handle. Anything else is filtered out before scoring, so no amount of
     * counter weighting can talk the bot into it.
     */
    private static boolean isLowGradeSummon(List<CardType> recipe, CardType mainCard) {
        return mainCard == CardType.Spawn
                && recipe.size() >= HOSPITALITY_MIN_CARDS
                && recipe.size() <= HOSPITALITY_MAX_CARDS;
    }

    static double explorationRate(BotTier tier) {
        return switch (tier) {
            // The hospitality bot is not a bad player, it is a player throwing the match on
            // purpose. Randomness would only take the choice back out of its hands.
            case HOSPITALITY -> 0.0;
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
