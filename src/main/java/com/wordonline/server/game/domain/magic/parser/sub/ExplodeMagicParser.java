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
        long id;
        if (cards.contains(CardType.Fire)) {
            prefabType = PrefabType.FireExplode;
            magicMaster = master;
            id = 19;
        } else if (cards.contains(CardType.Water)) {
            prefabType = PrefabType.WaterExplode;
            magicMaster = master;
            id = 20;
        } else if (cards.contains(CardType.Lightning)) {
            prefabType = PrefabType.ElectricExplode;
            magicMaster = master;
            id = 21;
        } else if (cards.contains(CardType.Nature)) {
            prefabType = PrefabType.LeafExplode;
            magicMaster = master;
            id = 22;
        } else if (cards.contains(CardType.Rock)) {
            prefabType = PrefabType.RockExplode;
            magicMaster = master;
            id = 23;
        } else if (cards.contains(CardType.Wind)) {
            prefabType = PrefabType.WindExplode;
            magicMaster = Master.None;
            id = 24;
        } else {
            return null;
        }
        return new Magic(id, CardType.Explode) {
            @Override
            public void run(GameLoop gameLoop) {
                new GameObject(magicMaster, prefabType, position.toVector3(), gameLoop);
            }
        };
    }
}
