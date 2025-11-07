package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BotBrain {

    public record InputDecision(List<CardType> playCards, Vector3 target) {}

    public InputDecision think(List<GameObject> gameObjectList, List<CardType> cardList, GameLoop loop, int mana)
    {
        try {
            Vector3 playerPos = GameConfig.RIGHT_PLAYER_POSITION;

            List<GameObject> enemies = gameObjectList.stream()
                    .filter(go -> go.getMaster() == Master.LeftPlayer)
                    .toList();

            List<CardType> magicList = cardList.stream()
                    .filter(card -> card.getType() == CardType.Type.Magic)
                    .toList();

            List<CardType> usableMagics = new ArrayList<>();

            for (CardType magic : magicList) {
                double range = loop.parameters.getValue(magic.name(), "range");
                int cost  = (int) loop.parameters.getValue(magic.name(), "mana_cost");

                if (cost > mana) continue;


                boolean inRange = enemies.stream().anyMatch(enemy ->
                        enemy.getPosition().distance(playerPos) <= range
                );

                if (inRange) {
                    usableMagics.add(magic);
                }
            }

            if (!usableMagics.isEmpty()) {
                GameObject nearest = nearestEnemy(enemies, playerPos);
                if (nearest != null) {
                    CardType useMagic = usableMagics.getFirst();
                    CardType useType = cardList.stream()
                            .filter(card -> card.getType() == CardType.Type.Type)
                            .toList().getFirst();
                    var inputCardList = new ArrayList<CardType>();
                    inputCardList.add(useMagic);
                    inputCardList.add(useType);

                    var target = nearest.getPosition();
                    return new InputDecision(inputCardList, target);
                }
            }
            List<CardType> spawnOrBuild = cardList.stream()
                    .filter(c -> c == CardType.Build || c == CardType.Spawn)
                    .toList();

            if(!spawnOrBuild.isEmpty())
            {
                CardType useMagic = spawnOrBuild.getFirst();
                CardType useType = cardList.stream()
                        .filter(card -> card.getType() == CardType.Type.Type)
                        .toList().getFirst();
                double range = loop.parameters.getValue(useMagic.name(), "range");
                var inputCardList = new ArrayList<CardType>();
                inputCardList.add(useMagic);
                inputCardList.add(useType);

                var target = randomPosInRange(GameConfig.RIGHT_PLAYER_POSITION, range);
                return new InputDecision(inputCardList, target);
            }
        } catch (Exception e) {
            log.trace(e.getMessage());
        }
        return null;
    }
    private static GameObject nearestEnemy(List<GameObject> enemies, Vector3 myPos) {
        GameObject best = null;
        double bestD = Double.MAX_VALUE;
        for (var e : enemies) {
            double d = myPos.distance(e.getPosition());
            if (d < bestD) { bestD = d; best = e; }
        }
        return best;
    }

    private static Vector3 randomPosInRange(Vector3 center, double range) {
        double u = ThreadLocalRandom.current().nextDouble();
        double r = Math.sqrt(u) * range;
        double theta = ThreadLocalRandom.current().nextDouble(Math.PI / 2, Math.PI * 3 / 2);

        double dx = r * Math.cos(theta);
        double dy = r * Math.sin(theta);
        return new Vector3((float)(center.getX() + dx), (float)(center.getY() + dy), center.getZ());
    }

}
