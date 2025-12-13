package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.domain.magic.parser.MagicParser;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class BotBrain {

    public record InputDecision(List<CardType> playCards, Vector3 target) {}

    private final MagicParser magicParser;

    public BotBrain(MagicParser magicParser) {
        this.magicParser = magicParser;
    }

    public InputDecision think(List<GameObject> gameObjectList,
                               List<CardType> cardList,
                               GameLoop loop,
                               int mana,
                               Master botSide)
    {
        try {
            Vector3 playerPos = (botSide == Master.LeftPlayer) 
                    ? GameConfig.LEFT_PLAYER_POSITION 
                    : GameConfig.RIGHT_PLAYER_POSITION;

            Master enemySide = (botSide == Master.LeftPlayer) 
                    ? Master.RightPlayer 
                    : Master.LeftPlayer;
            
            List<GameObject> enemies = gameObjectList.stream()
                    .filter(go -> go.getMaster() == enemySide)
                    .toList();

            if (cardList.isEmpty()) {
                return null;
            }

            List<MagicCandidate> offensive = new ArrayList<>();
            List<MagicCandidate> placement = new ArrayList<>();

            if (!(magicParser instanceof DatabaseMagicParser dbParser)) {
                log.warn("magicParser is not DatabaseMagicParser, fallback disabled");
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

                double range = loop.parameters.getValue(mainCard.name(), "range");
                int cost = (int) loop.parameters.getValue(mainCard.name(), "mana_cost");
                if (cost > mana) {
                    continue;
                }

                MagicCandidate candidate = new MagicCandidate(recipe, mainCard, range, cost);

                if (mainCard == CardType.Shoot || mainCard == CardType.Explode) {
                    offensive.add(candidate);
                } else {
                    placement.add(candidate);
                }
            }

            if (!offensive.isEmpty() && !enemies.isEmpty()) {
                List<MagicCandidate> usableOffensive = offensive.stream()
                        .filter(c -> enemies.stream().anyMatch(enemy ->
                                enemy.getPosition().distance(playerPos) <= c.range()))
                        .toList();

                if (!usableOffensive.isEmpty()) {
                    GameObject nearest = nearestEnemy(enemies, playerPos);
                    MagicCandidate chosen = usableOffensive.getFirst();

                    return new InputDecision(chosen.cards(), nearest.getPosition());
                }
            }

            if (!placement.isEmpty()) {
                MagicCandidate chosen = placement.getFirst();
                Vector3 target = randomPosInRange(playerPos, chosen.range());
                return new InputDecision(chosen.cards(), target);
            }

            CardType cycleCard = pickCycleCard(cardList, loop, mana);
            if (cycleCard != null) {
                double range = loop.parameters.getValue(cycleCard.name(), "range");
                Vector3 target = randomPosInRange(playerPos, range);
                return new InputDecision(List.of(cycleCard), target);
            }

        } catch (Exception e) {
            log.trace("Bot think error", e);
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

    private static GameObject nearestEnemy(List<GameObject> enemies, Vector3 myPos) {
        GameObject best = null;
        double bestD = Double.MAX_VALUE;
        for (var e : enemies) {
            double d = myPos.distance(e.getPosition());
            if (d < bestD) {
                bestD = d;
                best = e;
            }
        }
        return best;
    }

    private static Vector3 randomPosInRange(Vector3 center, double range) {
        double u = ThreadLocalRandom.current().nextDouble();
        double r = Math.sqrt(u) * range;
        double theta = ThreadLocalRandom.current().nextDouble(Math.PI / 2, Math.PI * 3 / 2);

        double dx = r * Math.cos(theta);
        double dy = r * Math.sin(theta);
        return new Vector3((float) (center.getX() + dx),
                (float) (center.getY() + dy),
                center.getZ());
    }

    private static CardType findMainCard(List<CardType> combo) {
        for (CardType c : combo) {
            if (c.getType() == CardType.Type.Magic) {
                return c;
            }
        }
        return combo.isEmpty() ? null : combo.getFirst();
    }

    private static CardType pickCycleCard(List<CardType> cardList, GameLoop loop, int mana) {
        List<CardType> candidates = new ArrayList<>();
        for (CardType c : cardList) {
            int cost = (int) loop.parameters.getValue(c.name(), "mana_cost");
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

    private record MagicCandidate(List<CardType> cards,
                                  CardType mainCard,
                                  double range,
                                  int cost) {}
}
