package com.wordonline.server.game.domain.magic.parser;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.Shot;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DummyMagicParser implements MagicParser {

    @Override
    public Magic parseMagic(List<CardType> cards, Master master, Vector2 position) {
        log.debug(cards.toString());
        if (cards.contains(CardType.Dummy)) {
            return new Magic(CardType.Dummy) {
                @Override
                public void run(GameLoop gameLoop) {
                    GameObject gameObject = new GameObject(master, PrefabType.Dummy, position, gameLoop);
                }
            };
        } else if (cards.contains(CardType.Summon)) {
            return new Magic(CardType.Summon) {
                @Override
                public void run(GameLoop gameLoop) {
                    new GameObject(master, PrefabType.FireSummon, position, gameLoop);
                }
            };
        } else if (cards.contains(CardType.Explode)) {
            return new Magic(CardType.Explode) {
                @Override
                public void run(GameLoop gameLoop) {
                    GameObject gameObject = new GameObject(master, PrefabType.FireExplode, position, gameLoop);
                }
            };
        } else if (cards.contains(CardType.Shoot)) {
            return new Magic(CardType.Shoot) {
                @Override
                public void run(GameLoop gameLoop) {
                    GameObject gameObject = new GameObject(
                            master,
                            PrefabType.FireShot,
                            GameConfig.PLAYER_POSITION.get(master),
                            gameLoop);
                    gameObject.getComponent(Shot.class).setTarget(position);
                }
            };
        }
        return null;

    }
}
