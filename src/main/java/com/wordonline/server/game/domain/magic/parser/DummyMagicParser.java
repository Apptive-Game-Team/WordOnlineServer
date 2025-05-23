package com.wordonline.server.game.domain.magic.parser;

import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.MagicComponent;
import com.wordonline.server.game.dto.Master;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Slf4j
public class DummyMagicParser implements MagicParser {

    @Override
    public Magic parseMagic(List<CardType> cards, Master master) {
        log.debug(cards.toString());
        if (cards.contains(CardType.Dummy)) {
            return (gameLoop) -> {
                GameObject gameObject = new GameObject(master, PrefabType.Dummy, new Vector2(10, 6), gameLoop);
            };
        } else if (cards.contains(CardType.Summon)) {
            return (gameLoop) -> {
                new GameObject(Master.RightPlayer, PrefabType.FireSummon, new Vector2(16, 8), gameLoop);
                new GameObject(Master.LeftPlayer, PrefabType.FireSummon, new Vector2(2, 6), gameLoop);
            };
        } else if (cards.contains(CardType.Drop)) {
            return (gameLoop) -> {
                GameObject gameObject = new GameObject(master, PrefabType.FireDrop, new Vector2(11, 6), gameLoop);
            };
        } else if (cards.contains(CardType.Explode)) {
            return (gameLoop) -> {
                GameObject gameObject = new GameObject(master, PrefabType.FireExplode, new Vector2(12, 6), gameLoop);
            };
        } else if (cards.contains(CardType.Shoot)) {
            return (gameLoop) -> {
                new GameObject(Master.LeftPlayer, PrefabType.FireShot, new Vector2(2, 5), gameLoop);
                new GameObject(Master.RightPlayer, PrefabType.FireShot, new Vector2(16, 5), gameLoop);
            };
        }
        return null;

    }
}
