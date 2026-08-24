package com.wordonline.server.game.domain.bot;

import com.wordonline.server.bot.domain.BotPersona;
import com.wordonline.server.bot.domain.BotTier;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.implement.explode.AbstractExplosionMagic;
import com.wordonline.server.game.domain.magic.implement.explode.OvergrowthMagic;
import com.wordonline.server.game.domain.magic.implement.shoot.VineTossMagic;
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

    static final String SEED_SPIRIT_RULE = "combo.seed-spirit";
    static final String MOB_CLUSTER_RULE = "combo.mob-cluster";
    static final String VALUE_RULE = "score.best-value";
    static final String CYCLE_RULE = "cycle.low-utility";
    static final double COMBO_CLUSTER_RADIUS = 2.5;
    static final int COMBO_CLUSTER_MIN_MOBS = 3;

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

    // Runs on the bot executor thread. Everything it reads about the world comes from the snapshot,
    // never from a live GameObject; parameters are loaded once per session and read-only after that.
    public InputDecision think(BotEye botEye, Parameters parameters, Master botSide)
    {
        return think(botEye, parameters, botSide, ThreadLocalRandom.current());
    }

    InputDecision think(BotEye botEye, Parameters parameters, Master botSide, Random random)
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
            Map<List<CardType>, Magic> recipeMap = dbParser.getAllMagicRecipeMap();
            Collection<List<CardType>> allRecipes = recipeMap.keySet();

            Optional<ScoredPlay> combo = findSeedSpiritCombo(
                    recipeMap, cardList, mana, spellStats, botEye.gameObjectList(), playerPos, botSide, random);
            if (combo.isEmpty()) {
                combo = findMobClusterCombo(
                        recipeMap, cardList, mana, spellStats, botEye.gameObjectList(), playerPos, enemySide, random);
            }
            if (combo.isPresent()) {
                ScoredPlay chosen = combo.get();
                log.debug("[Bot {}] Chose priority rule {}: {}", botSide, chosen.ruleId(), chosen.reason());
                return chosen.toDecision();
            }

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
                ScoredPlay chosen = choosePlay(plays);
                log.debug("[Bot {}] Chose {} at {} (score={}, cost={}, pressure={})",
                        botSide, chosen.recipe(), chosen.target(), chosen.score(), chosen.cost(), threats.pressure());
                return chosen.toDecision();
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
                                           BotSpellStats spellStats,
                                           ThreatAssessment threats,
                                           List<BotVisibleObject> enemies,
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
                        double score = value / cost;
                        return scored(recipe, impact.center(), cost, score, VALUE_RULE,
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

    /** Every candidate already carries exactly one tier-scaled random perturbation. */
    private ScoredPlay choosePlay(List<ScoredPlay> plays) {
        return plays.stream()
                .max(Comparator.comparingDouble(ScoredPlay::noisyScore))
                .orElse(plays.getFirst());
    }

    static double noiseAmplitude(BotTier tier) {
        return switch (tier) {
            case INTRO -> 0.50;
            case BEGINNER -> 0.30;
            case INTERMEDIATE -> 0.18;
            case ADVANCED -> 0.08;
            case ELITE -> 0.0;
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

    private Optional<ScoredPlay> findSeedSpiritCombo(Map<List<CardType>, Magic> recipeMap,
                                                      List<CardType> hand,
                                                      int mana,
                                                      BotSpellStats spellStats,
                                                      List<BotVisibleObject> objects,
                                                      Vector3 playerPos,
                                                      Master botSide,
                                                      Random random) {
        List<BotVisibleObject> seedSpirits = objects.stream()
                .filter(object -> object.master() == botSide)
                .filter(BotVisibleObject::targetable)
                .filter(object -> object.type() == PrefabType.SeedSpirit)
                .toList();
        if (seedSpirits.isEmpty()) {
            return Optional.empty();
        }

        List<ScoredPlay> candidates = new ArrayList<>();
        for (Map.Entry<List<CardType>, Magic> entry : recipeMap.entrySet()) {
            Magic magic = entry.getValue();
            if (!(magic instanceof VineTossMagic) && !(magic instanceof OvergrowthMagic)) {
                continue;
            }
            List<CardType> recipe = entry.getKey();
            int cost = spellStats.totalManaCost(recipe);
            CardType mainCard = findMainCard(recipe);
            if (!canMakeRecipe(hand, recipe) || cost > mana || mainCard == null) {
                continue;
            }
            double castRange = spellStats.castRange(mainCard);
            for (BotVisibleObject seedSpirit : seedSpirits) {
                if (seedSpirit.position().distance(playerPos) > castRange) {
                    continue;
                }
                String magicName = magic.getClass().getSimpleName();
                candidates.add(scored(recipe, seedSpirit.position(), cost, 1.0 / cost, SEED_SPIRIT_RULE,
                        magicName + " targets allied SeedSpirit " + seedSpirit.id() + ".", random));
            }
        }
        return candidates.stream().max(Comparator.comparingDouble(ScoredPlay::noisyScore));
    }

    private Optional<ScoredPlay> findMobClusterCombo(Map<List<CardType>, Magic> recipeMap,
                                                      List<CardType> hand,
                                                      int mana,
                                                      BotSpellStats spellStats,
                                                      List<BotVisibleObject> objects,
                                                      Vector3 playerPos,
                                                      Master enemySide,
                                                      Random random) {
        List<BotVisibleObject> enemyMobs = objects.stream()
                .filter(object -> object.master() == enemySide && object.mob() && object.targetable())
                .toList();
        if (enemyMobs.size() < COMBO_CLUSTER_MIN_MOBS) {
            return Optional.empty();
        }

        List<ScoredPlay> candidates = new ArrayList<>();
        for (Map.Entry<List<CardType>, Magic> entry : recipeMap.entrySet()) {
            if (!(entry.getValue() instanceof AbstractExplosionMagic)) {
                continue;
            }
            List<CardType> recipe = entry.getKey();
            int cost = spellStats.totalManaCost(recipe);
            CardType mainCard = findMainCard(recipe);
            if (!canMakeRecipe(hand, recipe) || cost > mana || mainCard == null) {
                continue;
            }
            findBestMobCluster(enemyMobs, playerPos, spellStats.castRange(mainCard)).ifPresent(cluster ->
                    candidates.add(scored(recipe, cluster.center(), cost, (double) cluster.count() / cost,
                            MOB_CLUSTER_RULE,
                            entry.getValue().getClass().getSimpleName() + " targets a cluster of "
                                    + cluster.count() + " enemy mobs.", random)));
        }
        return candidates.stream().max(Comparator.comparingDouble(ScoredPlay::noisyScore));
    }

    private static Optional<MobCluster> findBestMobCluster(List<BotVisibleObject> mobs,
                                                            Vector3 playerPos,
                                                            double castRange) {
        MobCluster best = null;
        for (BotVisibleObject anchor : mobs) {
            List<BotVisibleObject> cluster = mobs.stream()
                    .filter(mob -> mob.position().distance(anchor.position()) <= COMBO_CLUSTER_RADIUS)
                    .toList();
            if (cluster.size() < COMBO_CLUSTER_MIN_MOBS) {
                continue;
            }
            Vector3 initialCenter = averagePosition(cluster);
            cluster = mobs.stream()
                    .filter(mob -> mob.position().distance(initialCenter) <= COMBO_CLUSTER_RADIUS)
                    .toList();
            if (cluster.size() < COMBO_CLUSTER_MIN_MOBS) {
                continue;
            }
            Vector3 center = averagePosition(cluster);
            if (center.distance(playerPos) > castRange) {
                continue;
            }
            MobCluster candidate = new MobCluster(center, cluster.size());
            if (best == null || candidate.count() > best.count()
                    || (candidate.count() == best.count()
                    && candidate.center().distance(playerPos) < best.center().distance(playerPos))) {
                best = candidate;
            }
        }
        return Optional.ofNullable(best);
    }

    private static Vector3 averagePosition(List<BotVisibleObject> objects) {
        float x = 0;
        float y = 0;
        float z = 0;
        for (BotVisibleObject object : objects) {
            x += object.position().getX();
            y += object.position().getY();
            z += object.position().getZ();
        }
        float count = objects.size();
        return new Vector3(x / count, y / count, z / count);
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

    private record MobCluster(Vector3 center, int count) {}

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
