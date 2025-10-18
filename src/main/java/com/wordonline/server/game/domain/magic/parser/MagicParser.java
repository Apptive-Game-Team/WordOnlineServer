package com.wordonline.server.game.domain.magic.parser;

import java.util.List;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;

public interface MagicParser {
    // This method is used to parse the magic cards
    Magic parseMagic(List<CardType> cards);
}
