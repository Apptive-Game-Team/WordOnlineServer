package com.wordonline.server.game.domain.magic.parser;

import java.util.List;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.dto.Master;

public class ExtendedMagicParser implements MagicParser {

    private final MagicParser basicMagicParser = new BasicMagicParser();
    private final MagicParser hashMapMagicParser = new HashMapMagicParser();

    @Override
    public Magic parseMagic(List<CardType> cards, Master master, Vector2 position) {
        if (cards.size() == 2) {
            return basicMagicParser.parseMagic(cards, master, position);
        }

        return hashMapMagicParser.parseMagic(cards, master, position);
    }
}
