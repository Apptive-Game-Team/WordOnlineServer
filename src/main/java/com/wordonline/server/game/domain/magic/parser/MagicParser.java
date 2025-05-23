package com.wordonline.server.game.domain.magic.parser;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.dto.Master;

import java.util.List;

public interface MagicParser {
    // This method is used to parse the magic cards
    Magic parseMagic(List<CardType> cards, Master master, Vector2 position);
}
