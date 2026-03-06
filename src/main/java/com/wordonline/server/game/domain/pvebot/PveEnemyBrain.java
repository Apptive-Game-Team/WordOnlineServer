package com.wordonline.server.game.domain.pvebot;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.wordonline.server.game.domain.bot.BotSideUtil;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PveEnemyBrain {

    public record InputDecision(Magic magic, Vector3 target) {}

    public InputDecision think(List<GameObject> gameObjectList,
                               List<Magic> availableMagics,
                               Master botSide,
                               GameLoop loop) {
        if (availableMagics.isEmpty()) {
            return null;
        }

        try {
            Vector3 playerPos = BotSideUtil.getPlayerPosition(botSide);
            Master enemySide = BotSideUtil.getEnemySide(botSide);

            List<GameObject> enemies = gameObjectList.stream()
                    .filter(go -> go.getMaster() == enemySide)
                    .toList();

            List<Magic> offensive = availableMagics.stream()
                    .filter(magic -> isOffensive(magic.magicType))
                    .sorted(Comparator.comparingInt((Magic m) -> manaCostOf(loop, m)).reversed())
                    .toList();

            for (Magic magic : offensive) {
                GameObject nearestEnemyInRange = nearestEnemyInRange(enemies, playerPos, rangeOf(loop, magic));
                if (nearestEnemyInRange != null) {
                    return new InputDecision(magic, nearestEnemyInRange.getPosition());
                }
            }

            List<Magic> placement = availableMagics.stream()
                    .filter(magic -> !isOffensive(magic.magicType))
                    .sorted(Comparator.comparingInt((Magic m) -> manaCostOf(loop, m)).reversed())
                    .toList();

            if (!placement.isEmpty()) {
                Magic chosen = placement.getFirst();
                Vector3 target = randomPosInRange(playerPos, rangeOf(loop, chosen), botSide);
                return new InputDecision(chosen, target);
            }
        } catch (Exception e) {
            log.trace("PveEnemyBrain think error", e);
        }

        return null;
    }

    private static boolean isOffensive(CardType magicType) {
        return magicType == CardType.Shoot || magicType == CardType.Explode || magicType == CardType.Drop;
    }

    private static int manaCostOf(GameLoop loop, Magic magic) {
        return (int) loop.parameters.getValue(magic.magicType.name(), "mana_cost");
    }

    private static double rangeOf(GameLoop loop, Magic magic) {
        return loop.parameters.getValue(magic.magicType.name(), "range");
    }

    private static GameObject nearestEnemyInRange(List<GameObject> enemies, Vector3 myPos, double range) {
        GameObject best = null;
        double bestD = Double.MAX_VALUE;

        for (GameObject enemy : enemies) {
            double distance = myPos.distance(enemy.getPosition());
            if (distance <= range && distance < bestD) {
                best = enemy;
                bestD = distance;
            }
        }

        return best;
    }

    private static Vector3 randomPosInRange(Vector3 center, double range, Master botSide) {
        double u = ThreadLocalRandom.current().nextDouble();
        double r = Math.sqrt(u) * range;

        double theta;
        if (botSide == Master.RightPlayer) {
            theta = ThreadLocalRandom.current().nextDouble(Math.PI / 2, Math.PI * 3 / 2);
        } else if (botSide == Master.LeftPlayer) {
            theta = ThreadLocalRandom.current().nextDouble(-Math.PI / 2, Math.PI / 2);
        } else {
            throw new IllegalStateException("Unknown botSide: " + botSide);
        }

        double dx = r * Math.cos(theta);
        double dy = r * Math.sin(theta);

        return new Vector3((float) (center.getX() + dx),
                (float) (center.getY() + dy),
                center.getZ());
    }
}
