package com.wordonline.server.game.domain.magic.parser;

import java.util.List;
import java.util.Map;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;

public class HashMapMagicParser implements MagicParser {

    private static final Map<List<CardType>, Magic> magicHashMap;

    static {
        magicHashMap = Map.ofEntries(
                // 지대공 포탑
                createMagicEntry(List.of(CardType.Build, CardType.Explode, CardType.Rock),
                        new Magic(CardType.Build) {
                            @Override
                            public void run(GameLoop gameLoop) {

                            }}),
                // 지대지 대포
                createMagicEntry(List.of(CardType.Build, CardType.Shoot, CardType.Rock),
                        new Magic(CardType.Build) {
                            @Override
                            public void run(GameLoop gameLoop) {

                            }}),
                // 마나 회복 건물
                createMagicEntry(List.of(CardType.Build, CardType.Nature, CardType.Lightning),
                        new Magic(CardType.Build) {
                            @Override
                            public void run(GameLoop gameLoop) {

                            }}),
                // 물 원거리 유닛
                createMagicEntry(List.of(CardType.Spawn, CardType.Shoot, CardType.Water),
                        new Magic(CardType.Spawn) {
                            @Override
                            public void run(GameLoop gameLoop) {

                            }}),
                // 바위 골램
                createMagicEntry(List.of(CardType.Spawn, CardType.Rock, CardType.Rock),
                        new Magic(CardType.Spawn) {
                            @Override
                            public void run(GameLoop gameLoop) {

                            }}),
                // 번개 돌격병
                createMagicEntry(List.of(CardType.Spawn, CardType.Water, CardType.Lightning),
                        new Magic(CardType.Spawn) {
                            @Override
                            public void run(GameLoop gameLoop) {

                            }}),
                // 공중 번개 정령
                createMagicEntry(List.of(CardType.Spawn, CardType.Wind, CardType.Lightning),
                        new Magic(CardType.Spawn) {
                            @Override
                            public void run(GameLoop gameLoop) {

                            }}),
                // 불의 정령
                createMagicEntry(List.of(CardType.Spawn, CardType.Wind, CardType.Fire),
                        new Magic(CardType.Spawn) {
                            @Override
                            public void run(GameLoop gameLoop) {

                            }})
        );
    }

    private static Map.Entry<List<CardType>, Magic> createMagicEntry(List<CardType> cards, Magic magic) {
        return Map.entry(
                List.copyOf(
                        cards.stream()
                                .sorted()
                                .toList()
                ),
                magic
        );
    }

    @Override
    public Magic parseMagic(List<CardType> cards, Master master, Vector2 position) {
        List<CardType> key = List.copyOf(cards.stream().sorted().toList());
        return magicHashMap.get(key);
    }
}
