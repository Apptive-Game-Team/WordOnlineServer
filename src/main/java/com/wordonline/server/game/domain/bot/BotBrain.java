package com.wordonline.server.game.domain.bot;

import com.wordonline.server.bot.domain.BotPersona;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.parser.MagicParser;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.bot.BotCounterEvaluator;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class BotBrain {

    public record InputDecision(List<CardType> playCards, Vector3 target) {}

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
        List<CardType> cardList = botEye.cardList();
        int mana = botEye.mana();
        try {
            log.trace("[Bot {}] Start thinking with cards={}, mana={}", botSide, cardList, mana);
            Vector3 playerPos = BotSideUtil.getPlayerPosition(botSide);
            Master enemySide = BotSideUtil.getEnemySide(botSide);

            List<BotVisibleObject> enemies = botEye.gameObjectList().stream()
                    .filter(go -> go.master() == enemySide)
                    .toList();

            if (cardList.isEmpty()) {
                return null;
            }

            List<MagicCandidate> offensive = new ArrayList<>();
            List<MagicCandidate> placement = new ArrayList<>();
            boolean hasMakeableRecipe = false;
            boolean hasAffordableRecipe = false;

            if (!(magicParser instanceof DatabaseMagicParser dbParser)) {
                log.warn("[Bot {}] magicParser is not DatabaseMagicParser, fallback disabled", botSide);
                return null;
            }

            Collection<List<CardType>> allRecipes = dbParser.getAllMagicRecipes();
            List<List<CardType>> sortedRecipes = new ArrayList<>(allRecipes);
            sortedRecipes.sort((a, b) -> Integer.compare(b.size(), a.size()));

            for (List<CardType> recipe : sortedRecipes) {
                if (!canMakeRecipe(cardList, recipe)) {
                    continue;
                }

                CardType mainCard = findMainCard(recipe);
                if (mainCard == null) {
                    continue;
                }

                hasMakeableRecipe = true;
                double range = parameters.getValue(mainCard.name(), "range");
                int cost = (int) parameters.getValue(mainCard.name(), "mana_cost");
                if (cost > mana) {
                    continue;
                }

                hasAffordableRecipe = true;
                double score = scoreCandidate(recipe, mainCard, mana, cost, enemies);
                MagicCandidate candidate = new MagicCandidate(recipe, mainCard, range, cost, score);

                if (mainCard == CardType.Shoot || mainCard == CardType.Explode) {
                    offensive.add(candidate);
                } else {
                    placement.add(candidate);
                }
            }

            if (!offensive.isEmpty() && !enemies.isEmpty()) {
                log.debug("[Bot {}] Found {} offensive candidates and {} enemies", botSide, offensive.size(), enemies.size());
                List<MagicCandidate> usableOffensive = offensive.stream()
                        .filter(c -> enemies.stream().anyMatch(enemy ->
                                enemy.position().distance(playerPos) <= c.range()))
                        .toList();

                if (!usableOffensive.isEmpty()) {
                    BotVisibleObject nearest = nearestEnemy(enemies, playerPos);
                    MagicCandidate chosen = bestCandidate(usableOffensive);

                    log.info("[Bot {}] Chose offensive action: {} targeting nearest enemy at {}", botSide, chosen.cards(), nearest.position());
                    return new InputDecision(chosen.cards(), nearest.position());
                } else {
                    log.debug("[Bot {}] No offensive candidates in range", botSide);
                }
            }

            if (!placement.isEmpty()) {
                MagicCandidate chosen = bestCandidate(placement);
                Vector3 target = randomPosInRange(playerPos, chosen.range(), botSide);
                log.info("[Bot {}] Chose placement action: {} at random target {}", botSide, chosen.cards(), target);
                return new InputDecision(chosen.cards(), target);
            }

            if (hasMakeableRecipe && !hasAffordableRecipe) {
                log.debug("[Bot {}] Waiting for mana; makeable recipes exist but none are affordable. mana={}", botSide, mana);
                return null;
            }

            CardType cycleCard = pickCycleCard(cardList, parameters, mana);
            if (cycleCard != null) {
                double range = parameters.getValue(cycleCard.name(), "range");
                Vector3 target = randomPosInRange(playerPos, range, botSide);
                log.info("[Bot {}] Chose to cycle card: {} at random target {}", botSide, cycleCard, target);
                return new InputDecision(List.of(cycleCard), target);
            }

            log.trace("[Bot {}] No valid actions found this tick", botSide);
        } catch (Exception e) {
            log.error("[Bot " + botSide + "] Bot think error", e);
        }
        return null;
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

    private static BotVisibleObject nearestEnemy(List<BotVisibleObject> enemies, Vector3 myPos) {
        BotVisibleObject best = null;
        double bestD = Double.MAX_VALUE;
        for (var e : enemies) {
            double d = myPos.distance(e.position());
            if (d < bestD) {
                bestD = d;
                best = e;
            }
        }
        return best;
    }

    private static Vector3 randomPosInRange(Vector3 center, double range, Master botSide) {
        double u = ThreadLocalRandom.current().nextDouble();
        double r = Math.sqrt(u) * range;

        double theta = Double.NaN;

        if (botSide == Master.RightPlayer) {
            theta = ThreadLocalRandom.current()
                    .nextDouble(Math.PI / 2, Math.PI * 3 / 2);
        } else if (botSide == Master.LeftPlayer) {
            theta = ThreadLocalRandom.current()
                    .nextDouble(-Math.PI / 2, Math.PI / 2);
        }

        if (Double.isNaN(theta)) {
            throw new IllegalStateException("Unknown botSide: " + botSide);
        }

        double dx = r * Math.cos(theta);
        double dz = r * Math.sin(theta);
        return new Vector3((float) (center.getX() + dx),
                center.getY(),
                (float) (center.getZ() + dz));
    }

    private static CardType findMainCard(List<CardType> combo) {
        for (CardType c : combo) {
            if (c.getType() == CardType.Type.Magic) {
                return c;
            }
        }
        return combo.isEmpty() ? null : combo.getFirst();
    }

    private static CardType pickCycleCard(List<CardType> cardList, Parameters parameters, int mana) {
        List<CardType> candidates = new ArrayList<>();
        for (CardType c : cardList) {
            int cost = (int) parameters.getValue(c.name(), "mana_cost");
            if (cost <= mana) {
                candidates.add(c);
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        int idx = ThreadLocalRandom.current().nextInt(candidates.size());
        return candidates.get(idx);
    }

    private MagicCandidate bestCandidate(List<MagicCandidate> candidates) {
        return candidates.stream()
                .max((a, b) -> Double.compare(a.score(), b.score()))
                .orElse(candidates.getFirst());
    }

    private double scoreCandidate(List<CardType> recipe,
                                  CardType mainCard,
                                  int mana,
                                  int cost,
                                  List<BotVisibleObject> enemies) {
        double baseScore = recipe.size();
        if (mainCard == CardType.Shoot || mainCard == CardType.Explode) {
            baseScore += 10.0;
        } else {
            baseScore += 5.0;
        }

        double counterScore = counterEvaluator.evaluate(recipe, enemies) * persona.normalizedCounterAggression();
        double manaEfficiencyScore = Math.max(0, mana - cost) * 0.01;
        double tierSkillBonus = switch (persona.tier()) {
            case INTRO -> 0.0;
            case BEGINNER -> 0.2;
            case INTERMEDIATE -> 0.4;
            case ADVANCED -> 0.7;
            case ELITE -> 1.0;
        };
        return baseScore + counterScore + manaEfficiencyScore + tierSkillBonus;
    }

    private record MagicCandidate(List<CardType> cards,
                                  CardType mainCard,
                                  double range,
                                  int cost,
                                  double score) {}
}
