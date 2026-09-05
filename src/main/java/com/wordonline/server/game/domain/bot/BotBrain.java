package com.wordonline.server.game.domain.bot;

import com.wordonline.server.bot.domain.BotPersona;
import com.wordonline.server.bot.domain.BotTier;
import com.wordonline.server.game.domain.bot.rule.BotRuleBook;
import com.wordonline.server.game.domain.bot.rule.BotRuleEngine;
import com.wordonline.server.game.domain.bot.rule.PriorityBotRuleEngine;
import com.wordonline.server.game.domain.bot.rule.RecipeOption;
import com.wordonline.server.game.domain.bot.rule.RuleOutcome;
import com.wordonline.server.game.domain.bot.view.BotMemory;
import com.wordonline.server.game.domain.bot.view.BotWorldView;
import com.wordonline.server.game.domain.bot.view.BotWorldViewFactory;
import com.wordonline.server.game.domain.bot.view.GameObjectTags;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
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
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
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

    public record InputDecision(List<CardType> playCards, Vector3 target, String ruleId, String reason) {}

    static final String VALUE_RULE = "score.best-value";
    static final String CYCLE_RULE = "cycle.low-utility";

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

    /** The declared tactics. Consulted before the value scorer; silent when nothing applies. */
    private final BotRuleEngine ruleEngine;

    private final BotWorldViewFactory worldViewFactory;

    /**
     * Belongs to this brain alone. It is what lets a rule ask whether something is coming at the
     * bot: velocity has to be differenced across think passes, so it cannot be a local.
     */
    private final BotMemory memory = new BotMemory();

    public BotBrain(MagicParser magicParser, BotCounterEvaluator counterEvaluator, BotPersona persona) {
        this(magicParser, counterEvaluator, persona, HospitalityDirector.DEFAULT_BOARD_SHARE);
    }

    public BotBrain(MagicParser magicParser,
                    BotCounterEvaluator counterEvaluator,
                    BotPersona persona,
                    double opponentNoviceProgress) {
        this(magicParser, counterEvaluator, persona, opponentNoviceProgress, GameObjectTags.none());
    }

    /**
     * @param opponentNoviceProgress how far the human opponent is through the tutorial; the share of
     *                               their board a hospitality bot may match. Ignored by other tiers.
     */
    /**
     * @param gameObjectTags prefab tag lookup for the rule layer. A bot built without one still
     *                       plays; its tag-based conditions simply never match.
     */
    public BotBrain(MagicParser magicParser,
                    BotCounterEvaluator counterEvaluator,
                    BotPersona persona,
                    double opponentNoviceProgress,
                    GameObjectTags gameObjectTags) {
        this.magicParser = magicParser;
        this.counterEvaluator = counterEvaluator;
        this.persona = persona;
        this.hospitalityDirector = persona.tier() == BotTier.HOSPITALITY
                ? new HospitalityDirector(opponentNoviceProgress)
                : null;
        this.ruleEngine = new PriorityBotRuleEngine(BotRuleBook.defaultRules());
        this.worldViewFactory = new BotWorldViewFactory(
                gameObjectTags, BotRuleBook.CLUSTER_RADIUS, BotRuleBook.CLUSTER_MIN_MOBS);
    }

    // Runs on the bot executor thread. Everything it reads about the world comes from the snapshot,
    // never from a live GameObject; parameters are loaded once per session and read-only after that.
    public InputDecision think(BotEye botEye, Parameters parameters, Master botSide) {
        return think(botEye, parameters, botSide, false, ThreadLocalRandom.current());
    }

    /**
     * @param overdue the bot has been silent long enough that standing still would read as going
     *                easy on the player, so it must not hold out for a better moment
     */
    public InputDecision think(BotEye botEye, Parameters parameters, Master botSide, boolean overdue)
    {
        return think(botEye, parameters, botSide, overdue, ThreadLocalRandom.current());
    }

    InputDecision think(BotEye botEye, Parameters parameters, Master botSide, Random random) {
        return think(botEye, parameters, botSide, false, random);
    }

    private InputDecision think(BotEye botEye,
                                Parameters parameters,
                                Master botSide,
                                boolean overdue,
                                Random random) {
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
            double aggression = aggression(botEye, threats, botSide);

            // 접대 봇의 보드는 플레이어 보드의 일정 비율 아래로만 간다. 한 번의 소환만 비교하면
            // 싼 유닛을 계속 쌓아 결국 따라붙으므로, 이미 깔아 둔 것까지 더해서 본다. 비율과
            // 마나로 재는 이유는 각각 HospitalityDirector와 BoardValue에 적어 두었다.
            int manaBudget = Integer.MAX_VALUE;
            if (hospitalityDirector != null) {
                BoardValue boardValue = new BoardValue(dbParser, spellStats, parameters);
                int enemyBoardMana = boardValue.manaOnField(botEye.gameObjectList(), enemySide);
                int ownBoardMana = boardValue.manaOnField(botEye.gameObjectList(), botSide);
                manaBudget = hospitalityDirector.summonAllowance(enemyBoardMana, ownBoardMana);
                log.debug("[Bot {}] Hospitality board: enemy={} own={} budget={}",
                        botSide, enemyBoardMana, ownBoardMana, manaBudget);
            }

            Map<List<CardType>, Magic> recipeMap = dbParser.getAllMagicRecipeMap();
            Collection<List<CardType>> allRecipes = dbParser.getAllMagicRecipes();
            if ((allRecipes == null || allRecipes.isEmpty()) && recipeMap != null) {
                allRecipes = recipeMap.keySet();
            }

            // The declared tactics get first refusal. Whether the tutorial bot is excluded is a
            // property of each rule now (they are all gated above HOSPITALITY), not an if here.
            Optional<ScoredPlay> tactic = applyRules(botEye, botSide, spellStats, recipeMap, random);
            if (tactic.isPresent()) {
                ScoredPlay chosen = tactic.get();
                log.debug("[Bot {}] Chose priority rule {}: {}", botSide, chosen.ruleId(), chosen.reason());
                return chosen.toDecision();
            }

            List<ScoredPlay> plays = new ArrayList<>();
            boolean hasMakeableRecipe = false;
            boolean hasAffordableRecipe = false;

            for (List<CardType> recipe : allRecipes) {
                if (!RecipeMatcher.canMake(cardList, recipe)) {
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

                // 이번 소환이 놓인 뒤에도 봇 보드가 플레이어 보드보다 약해야 한다. 지킬 수 없으면
                // 소환하지 않는다 - 데드라인이 있어도 이 규칙은 깨지 않는다. 플레이어보다 센 것을
                // 한 번 내놓는 순간 접대는 실패하고, 그건 잠깐 조용한 것보다 나쁘다.
                if (cost >= manaBudget) {
                    continue;
                }

                buildPlay(recipe, mainCard, cost, aggression, spellStats, threats, enemies, playerPos, botSide, random)
                        .ifPresent(plays::add);
            }

            if (!plays.isEmpty()) {
                ScoredPlay chosen = choosePlay(plays);
                log.debug("[Bot {}] Chose {} at {} (score={}, cost={}, pressure={})",
                        botSide, chosen.recipe(), chosen.target(), chosen.score(), chosen.cost(), threats.pressure());
                return chosen.toDecision();
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
                return new InputDecision(
                        List.of(cycleCard), target, CYCLE_RULE,
                        "No complete affordable recipe; cycling the least-used card " + cycleCard + ".");
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
                        return scored(recipe, impact.center(), cost, value / cost, VALUE_RULE,
                                "Best offensive value covers " + impact.coveredCount()
                                        + " target(s) with expected damage " + impact.expectedDamage() + ".",
                                random);
                    });
        }

        double value = PLACEMENT_VALUE_PER_CARD * recipe.size()
                * (1 + DEFENSE_URGENCY_WEIGHT * threats.pressure())
                + counterValue;
        Vector3 target = PlacementPlanner.plan(playerPos, castRange, botSide, threats, random);
        return Optional.of(scored(recipe, target, cost, value / cost, VALUE_RULE,
                "Best placement value for current defensive pressure " + threats.pressure() + ".", random));
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

    /** Every candidate already carries exactly one tier-scaled random perturbation. */
    private ScoredPlay choosePlay(List<ScoredPlay> plays) {
        return plays.stream()
                .max(Comparator.comparingDouble(ScoredPlay::noisyScore))
                .orElse(plays.getFirst());
    }

    static double noiseAmplitude(BotTier tier) {
        return switch (tier) {
            case HOSPITALITY, ELITE -> 0.0;
            case INTRO -> 0.50;
            case BEGINNER -> 0.30;
            case INTERMEDIATE -> 0.18;
            case ADVANCED -> 0.08;
        };
    }

    private ScoredPlay scored(List<CardType> recipe,
                              Vector3 target,
                              int cost,
                              double score,
                              String ruleId,
                              String reason,
                              Random random) {
        double amplitude = noiseAmplitude(persona.tier());
        double noisyScore = score * (1.0 + (random.nextDouble() * 2.0 - 1.0) * amplitude);
        return new ScoredPlay(recipe, target, cost, score, noisyScore, ruleId, reason);
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

    /**
     * Runs the declared tactics and turns the winner into a scored play.
     *
     * <p>The engine hands back every candidate of the one rule that matched and picks no winner:
     * the tier-scaled perturbation below has to be applied exactly once per candidate, and it
     * lives here, on the same side of the seam as the value scorer's. A rule that scored itself
     * would either skip that noise or get it twice.
     */
    private Optional<ScoredPlay> applyRules(BotEye botEye,
                                            Master botSide,
                                            BotSpellStats spellStats,
                                            Map<List<CardType>, Magic> recipeMap,
                                            Random random) {
        if (recipeMap == null || recipeMap.isEmpty()) {
            return Optional.empty();
        }

        BotWorldView view = worldViewFactory.build(botEye, botSide, persona.tier(), memory);
        List<RecipeOption> options = castableOptions(recipeMap, botEye.cardList(), botEye.mana(), spellStats);
        if (options.isEmpty()) {
            return Optional.empty();
        }

        List<RuleOutcome> outcomes = ruleEngine.evaluate(view, options, System.currentTimeMillis());
        return outcomes.stream()
                .map(outcome -> scored(outcome.recipe(), outcome.target(), outcome.cost(),
                        outcome.score(), outcome.ruleId(), outcome.reason(), random))
                .max(Comparator.comparingDouble(ScoredPlay::noisyScore));
    }

    /**
     * Every recipe the hand can make and the bot can pay for, priced once.
     *
     * <p>Each tactic used to redo this filter for itself. Doing it here means a rule states only
     * its tactic and cannot propose a cast the input handler would turn away.
     */
    private static List<RecipeOption> castableOptions(Map<List<CardType>, Magic> recipeMap,
                                                      List<CardType> hand,
                                                      int mana,
                                                      BotSpellStats spellStats) {
        List<RecipeOption> options = new ArrayList<>();
        for (Map.Entry<List<CardType>, Magic> entry : recipeMap.entrySet()) {
            List<CardType> recipe = entry.getKey();
            CardType mainCard = findMainCard(recipe);
            if (mainCard == null || !RecipeMatcher.canMake(hand, recipe)) {
                continue;
            }
            int cost = spellStats.totalManaCost(recipe);
            if (cost > mana) {
                continue;
            }
            options.add(new RecipeOption(
                    recipe,
                    entry.getValue(),
                    mainCard,
                    cost,
                    spellStats.castRange(mainCard),
                    spellStats.blastRadius(mainCard),
                    spellStats.damagePerTarget(mainCard)));
        }
        return options;
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

    private record ScoredPlay(List<CardType> recipe,
                              Vector3 target,
                              int cost,
                              double score,
                              double noisyScore,
                              String ruleId,
                              String reason) {
        private InputDecision toDecision() {
            return new InputDecision(recipe, target, ruleId, reason);
        }
    }
}
