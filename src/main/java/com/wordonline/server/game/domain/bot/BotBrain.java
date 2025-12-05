package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.parser.MagicParser;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
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
                               int mana)
    {
        try {
            Vector3 playerPos = GameConfig.RIGHT_PLAYER_POSITION;

            List<GameObject> enemies = gameObjectList.stream()
                    .filter(go -> go.getMaster() == Master.LeftPlayer)
                    .toList();

            if (cardList.isEmpty()) {
                return null;
            }

            // 1. 현재 손패(cardList)로 만들 수 있는 모든 마법 조합 찾기
            List<MagicCandidate> offensive = new ArrayList<>();
            List<MagicCandidate> placement = new ArrayList<>();

            for (List<CardType> combo : allCombinations(cardList)) {
                // 이 조합으로 실제 마법이 정의되어 있지 않으면 패스
                if (magicParser.parseMagic(combo) == null) {
                    continue;
                }

                CardType mainCard = findMainCard(combo);
                if (mainCard == null) {
                    continue;
                }

                double range = loop.parameters.getValue(mainCard.name(), "range");
                int cost = (int) loop.parameters.getValue(mainCard.name(), "mana_cost");
                if (cost > mana) {
                    continue;
                }

                MagicCandidate candidate = new MagicCandidate(combo, mainCard, range, cost);

                // 메인 카드 기준으로 공격 / 설치 계열 분리
                if (mainCard == CardType.Shoot || mainCard == CardType.Explode) {
                    offensive.add(candidate);
                } else {
                    placement.add(candidate);
                }
            }

            // 2. 공격 마법: 사거리 안에 적이 있을 때만 사용
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

            // 3. 설치 / 소환 계열 (사거리 안 랜덤 위치에 사용)
            if (!placement.isEmpty()) {
                MagicCandidate chosen = placement.getFirst();
                Vector3 target = randomPosInRange(playerPos, chosen.range());
                return new InputDecision(chosen.cards(), target);
            }

            // 4. 여기까지 왔다는 건, 어떤 조합으로도 마법을 못 쓴다는 뜻
            //    → 아래 2번 요구사항: 카드 순환 로직으로 넘어감
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

    // 현재 손패로 만들 수 있는 모든 부분집합 (size >= 2)
    private static List<List<CardType>> allCombinations(List<CardType> cards) {
        int n = cards.size();
        List<List<CardType>> result = new ArrayList<>();
        int maxMask = 1 << n;

        // 비트마스크로 부분집합 생성
        for (int mask = 1; mask < maxMask; mask++) {
            if (Integer.bitCount(mask) < 2) continue; // 1장짜리는 버린다
            List<CardType> combo = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    combo.add(cards.get(i));
                }
            }
            result.add(combo);
        }
        return result;
    }

    // 해당 조합에서 "메인" 카드 (사거리/코스트를 결정하는 카드) 추출
    private static CardType findMainCard(List<CardType> combo) {
        // 1순위: Magic 타입 카드
        for (CardType c : combo) {
            if (c.getType() == CardType.Type.Magic) {
                return c;
            }
        }
        // 그 외에는 그냥 첫 카드
        return combo.isEmpty() ? null : combo.getFirst();
    }

    // 2번 요구사항: 순환용 카드 하나 뽑기
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
