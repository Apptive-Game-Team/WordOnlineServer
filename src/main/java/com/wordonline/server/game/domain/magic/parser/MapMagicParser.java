package com.wordonline.server.game.domain.magic.parser;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapMagicParser implements MagicParser {

    private static final Map<List<CardType>, BiFunction<Master, Vector2, Magic>> magicHashMap;

    static {
        magicHashMap = Map.ofEntries(
                // 지대공 포탑
                createMagicEntry(List.of(CardType.Build, CardType.Explode, CardType.Rock),
                        (master, position) -> new Magic(CardType.Build) {
                            @Override
                            public void run(GameLoop gameLoop) {

                            }}),
                // 지대지 대포
                createMagicEntry(List.of(CardType.Build, CardType.Shoot, CardType.Rock),
                        (master, position) -> new Magic(CardType.Build) {
                            @Override
                            public void run(GameLoop gameLoop) {
                                new GameObject(master, PrefabType.GroundCannon, position.toVector3(), gameLoop);
                            }}),
                // 마나 회복 건물
                createMagicEntry(List.of(CardType.Build, CardType.Nature, CardType.Lightning),
                        (master, position) -> new Magic(CardType.Build) {
                            @Override
                            public void run(GameLoop gameLoop) {
                                new GameObject(master, PrefabType.ManaWell, position.toVector3(), gameLoop);
                            }}),
                // 물 원거리 유닛
                createMagicEntry(List.of(CardType.Spawn, CardType.Shoot, CardType.Water),
                        (master, position) -> new Magic(CardType.Spawn) {
                            @Override
                            public void run(GameLoop gameLoop) {
                                new GameObject(master, PrefabType.AquaArcher, position.toVector3(), gameLoop);
                            }}),
                // 바위 골램
                createMagicEntry(List.of(CardType.Spawn, CardType.Rock, CardType.Rock),
                        (master, position) -> new Magic(CardType.Spawn) {
                            @Override
                            public void run(GameLoop gameLoop) {
                                new GameObject(master, PrefabType.RockGolem, position.toVector3(), gameLoop);
                            }}),
                // 번개 돌격병
                createMagicEntry(List.of(CardType.Spawn, CardType.Water, CardType.Lightning),
                        (master, position) -> new Magic(CardType.Spawn) {
                            @Override
                            public void run(GameLoop gameLoop) {
                                new GameObject(master, PrefabType.StormRider, position.toVector3(), gameLoop);
                            }}),
                // 공중 번개 정령
                createMagicEntry(List.of(CardType.Spawn, CardType.Wind, CardType.Lightning),
                        (master, position) -> new Magic(CardType.Spawn) {
                            @Override
                            public void run(GameLoop gameLoop) {

                            }}),
                // 불의 정령
                createMagicEntry(List.of(CardType.Spawn, CardType.Wind, CardType.Fire),
                        (master, position) -> new Magic(CardType.Spawn) {
                            @Override
                            public void run(GameLoop gameLoop) {
                                new GameObject(master, PrefabType.FireSpirit, position.toVector3(), gameLoop);
                            }})
        );
    }

    private static Map.Entry<List<CardType>, BiFunction<Master, Vector2, Magic>> createMagicEntry(
            List<CardType> cards,
            BiFunction<Master, Vector2, Magic> magic) {
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
        BiFunction<Master, Vector2, Magic> factory = magicHashMap.get(key);

        if (factory == null) {
            log.info("cards: {}, keys: {}", cards, key);

            return null;
        }

        return factory.apply(master, position);
    }
}
