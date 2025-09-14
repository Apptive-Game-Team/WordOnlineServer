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
        Master magicMaster;
        if (cards.contains(CardType.Fire)) {
            prefabType = PrefabType.FireExplode;
            magicMaster = master;
        } else if (cards.contains(CardType.Water)) {
            prefabType = PrefabType.WaterExplode;
            magicMaster = master;
        } else if (cards.contains(CardType.Lightning)) {
            prefabType = PrefabType.ElectricExplode;
            magicMaster = master;
        } else if (cards.contains(CardType.Leaf)) {
            prefabType = PrefabType.LeafExplode;
            magicMaster = master;
        } else if (cards.contains(CardType.Rock)) {
            prefabType = PrefabType.RockExplode;
            magicMaster = master;
        } else if (cards.contains(CardType.Wind)) {
            prefabType = PrefabType.WindExplode;
            magicMaster = Master.None;
        } else {
            return null;
        }
        return new Magic(CardType.Explode) {
            @Override
            public void run(GameLoop gameLoop) {
                new GameObject(magicMaster, prefabType, position.toVector3(), gameLoop);
            }
        };
    }
}
