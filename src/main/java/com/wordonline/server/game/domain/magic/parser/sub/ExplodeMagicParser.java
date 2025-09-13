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

public class ExplodeMagicParser implements MagicParser {

    @Override
    public Magic parseMagic(List<CardType> cards, Master master, Vector2 position) {
        PrefabType prefabType;
        if (cards.contains(CardType.Fire)) {
            prefabType = PrefabType.FireExplode;
        } else if (cards.contains(CardType.Water)) {
            prefabType = PrefabType.WaterExplode;
        } else if (cards.contains(CardType.Lightning)) {
            prefabType = PrefabType.ElectricExplode;
        } else if (cards.contains(CardType.Nature)) {
            prefabType = PrefabType.LeafExplode;
        } else if (cards.contains(CardType.Rock)) {
            prefabType = PrefabType.RockExplode;
        } else {
            return null;
        }
        return new Magic(CardType.Explode) {
            @Override
            public void run(GameLoop gameLoop) {
                new GameObject(master, prefabType, position.toVector3(), gameLoop);
            }
        };
    }
}
