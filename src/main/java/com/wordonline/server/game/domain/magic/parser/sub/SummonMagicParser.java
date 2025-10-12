package com.wordonline.server.game.domain.magic.parser.sub;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.MagicParser;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;

import java.util.List;

public class SummonMagicParser implements MagicParser {

    @Override
    public Magic parseMagic(List<CardType> cards, Master master, Vector2 position) {
        PrefabType prefabType;
        long id;

        if (cards.contains(CardType.Fire)) {
            prefabType = PrefabType.FireSummon;
            id = 13;
        } else if (cards.contains(CardType.Water)) {
            prefabType = PrefabType.WaterSummon;
            id = 14;
        } else if (cards.contains(CardType.Lightning)) {
            prefabType = PrefabType.ElectricSummon;
            id = 15;
        } else if (cards.contains(CardType.Nature)) {
            prefabType = PrefabType.LeafSummon;
            id = 16;
        } else if (cards.contains(CardType.Rock)) {
            prefabType = PrefabType.RockSummon;
            id = 17;
        } else if (cards.contains(CardType.Wind)) {
            prefabType = PrefabType.WindSummon;
            id = 18;
        } else {
            return null;
        }

        return new Magic(id, CardType.Build) {
            @Override
            public void run(GameLoop gameLoop) {
                new GameObject(master, prefabType, position.toVector3(), gameLoop);
            }
        };
    }
}
