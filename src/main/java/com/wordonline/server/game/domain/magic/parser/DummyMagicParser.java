package com.wordonline.server.game.domain.magic.parser;

import com.wordonline.server.game.domain.Position;
import com.wordonline.server.game.domain.PrefabType;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DummyMagicParser implements MagicParser {

    @Override
    public Magic parseMagic(List<CardType> cards) {
        log.debug(cards.toString());
        if (!cards.contains(CardType.Dummy))
            return null;

        return (gameLoop) -> {
            new GameObject(Master.None, PrefabType.Dummy, new Position(1, 1), gameLoop);
        };
    }

    private static final Logger log = LoggerFactory.getLogger(DummyMagicParser.class);
}
