package com.wordonline.server.game.domain.magic.parser;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.sub.ExplodeMagicParser;
import com.wordonline.server.game.domain.magic.parser.sub.ShootMagicParser;
import com.wordonline.server.game.domain.magic.parser.sub.SpawnMagicParser;
import com.wordonline.server.game.domain.magic.parser.sub.SummonMagicParser;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.dto.Master;

import java.util.ArrayList;
import java.util.List;

public class BasicMagicParser implements MagicParser {
    private final ExplodeMagicParser explodeMagicParser = new ExplodeMagicParser();
    private final ShootMagicParser shootMagicParser = new ShootMagicParser();
    private final SummonMagicParser summonMagicParser = new SummonMagicParser();
    private final SpawnMagicParser spawnMagicParser = new SpawnMagicParser();

    @Override
    public Magic parseMagic(List<CardType> cards, Master master, Vector2 position) {
        List<CardType> mutableCards = new ArrayList<>(cards);
        if (mutableCards.contains(CardType.Build)) {
            mutableCards.remove(CardType.Build);
            return summonMagicParser.parseMagic(mutableCards, master, position);
        } else if (mutableCards.contains(CardType.Explode)) {
            mutableCards.remove(CardType.Explode);
            return explodeMagicParser.parseMagic(mutableCards, master, position);
        } else if (mutableCards.contains(CardType.Shoot)) {
            mutableCards.remove(CardType.Shoot);
            return shootMagicParser.parseMagic(mutableCards, master, position);
        } else if (mutableCards.contains(CardType.Spawn)) {
            mutableCards.remove(CardType.Spawn);
            return spawnMagicParser.parseMagic(mutableCards, master, position);
        }
        return null;
    }
}
