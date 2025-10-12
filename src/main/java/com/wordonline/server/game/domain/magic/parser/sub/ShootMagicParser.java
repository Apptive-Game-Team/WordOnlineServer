package com.wordonline.server.game.domain.magic.parser.sub;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.MagicParser;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.component.magic.Shot;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;

import java.util.List;

public class ShootMagicParser implements MagicParser {

    @Override
    public Magic parseMagic(List<CardType> cards, Master master, Vector2 position) {
        PrefabType prefabType;
        Master magicMaster;
        long id;
        if (cards.contains(CardType.Fire)) {
            prefabType = PrefabType.FireShot;
            magicMaster = master;
            id = 7;
        } else if (cards.contains(CardType.Water)) {
            prefabType = PrefabType.WaterShot;
            magicMaster = master;
            id = 8;
        } else if (cards.contains(CardType.Lightning)) {
            prefabType = PrefabType.ElectricShot;
            magicMaster = master;
            id = 9;
        } else if (cards.contains(CardType.Nature)) {
            prefabType = PrefabType.LeafShot;
            magicMaster = master;
            id = 10;
        } else if (cards.contains(CardType.Rock)) {
            prefabType = PrefabType.RockShot;
            magicMaster = master;
            id = 11;
        } else if (cards.contains(CardType.Wind)) {
            prefabType = PrefabType.WindShot;
            magicMaster = Master.None;
            id = 12;
        } else {
            return null;
        }
        return new Magic(id, CardType.Shoot) {
            @Override
            public void run(GameLoop gameLoop) {
                GameObject gameObject = new GameObject(
                        magicMaster,
                        prefabType,
                        GameConfig.PLAYER_POSITION.get(master),
                        gameLoop);
                gameObject.getComponent(Shot.class).setTarget(position);
            }
        };
    }
}
