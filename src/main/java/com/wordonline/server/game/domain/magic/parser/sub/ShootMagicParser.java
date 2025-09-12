package com.wordonline.server.game.domain.magic.parser.sub;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.MagicParser;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.component.magic.Shot;
import com.wordonline.server.game.domain.object.component.mob.pathfinder.PathFinder;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;

import java.util.List;

public class ShootMagicParser implements MagicParser {

    @Override
    public Magic parseMagic(List<CardType> cards, Master master, Vector2 position) {
        PrefabType prefabType;
        if (cards.contains(CardType.Fire)) {
            prefabType = PrefabType.FireShot;
        } else if (cards.contains(CardType.Water)) {
            prefabType = PrefabType.WaterShot;
        } else if (cards.contains(CardType.Lightning)) {
            prefabType = PrefabType.ElectricShot;
        } else if (cards.contains(CardType.Leaf)) {
            prefabType = PrefabType.LeafShot;
        } else if (cards.contains(CardType.Rock)) {
            prefabType = PrefabType.RockShot;
        } else if (cards.contains(CardType.Wind)) {
            prefabType = PrefabType.WindShot;
        } else {
            return null;
        }
        return new Magic(CardType.Shoot) {
            @Override
            public void run(GameLoop gameLoop) {
                GameObject gameObject = new GameObject(
                        master,
                        prefabType,
                        GameConfig.PLAYER_POSITION.get(master),
                        gameLoop);
                gameObject.getComponent(Shot.class).setTarget(position);
            }
        };
    }
}
