package com.wordonline.server.game.domain.magic.parser;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.sub.ExplodeMagicParser;
import com.wordonline.server.game.domain.magic.parser.sub.ShootMagicParser;
import com.wordonline.server.game.domain.magic.parser.sub.SummonMagicParser;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.dto.Master;

import java.util.List;

public class BasicMagicParser implements MagicParser {
    ExplodeMagicParser explodeMagicParser = new ExplodeMagicParser();
    ShootMagicParser shootMagicParser = new ShootMagicParser();
    SummonMagicParser summonMagicParser = new SummonMagicParser();

    @Override
    public Magic parseMagic(List<CardType> cards, Master master, Vector2 position) {
        if (cards.contains(CardType.Summon)) {
            cards.remove(CardType.Summon);
            return summonMagicParser.parseMagic(cards, master, position);
        } else if (cards.contains(CardType.Explode)) {
            cards.remove(CardType.Explode);
            return explodeMagicParser.parseMagic(cards, master, position);
        } else if (cards.contains(CardType.Shoot)) {
            cards.remove(CardType.Shoot);
            return shootMagicParser.parseMagic(cards, master, position);
        }
        return null;
    }
}
