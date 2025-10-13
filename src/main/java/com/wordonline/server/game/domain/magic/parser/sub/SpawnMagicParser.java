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

public class SpawnMagicParser implements MagicParser {

    @Override
    public Magic parseMagic(List<CardType> cards, Master master, Vector2 position) {
        PrefabType prefabType;
        long id;

        if (cards.contains(CardType.Fire)) {
            prefabType = PrefabType.FireSlime;
            id = 1;
        } else if (cards.contains(CardType.Water)) {
            prefabType = PrefabType.WaterSlime;
            id = 2;
        } else if (cards.contains(CardType.Lightning)) {
            prefabType = PrefabType.ElectricSlime;
            id = 3;
        } else if (cards.contains(CardType.Nature)) {
            prefabType = PrefabType.LeafSlime;
            id = 4;
        } else if (cards.contains(CardType.Rock)) {
            prefabType = PrefabType.RockSlime;
            id = 5;
        } else if (cards.contains(CardType.Wind)) {
            prefabType = PrefabType.WindSlime;
            id = 6;
        } else {
            return null;
        }

        return new Magic(id, CardType.Spawn) {
            @Override
            public void run(GameLoop gameLoop) {
                new GameObject(master, prefabType, position.toVector3(), gameLoop);
                new GameObject(master, prefabType, position.plus(0.5f,0).toVector3(), gameLoop);
                new GameObject(master, prefabType, position.plus(-0.5f,0).toVector3(), gameLoop);
            }
        };
    }
}
