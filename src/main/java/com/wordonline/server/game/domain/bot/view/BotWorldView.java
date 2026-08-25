package com.wordonline.server.game.domain.bot.view;

import com.wordonline.server.bot.domain.BotTier;
import com.wordonline.server.game.domain.bot.ThreatAssessment;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;

import java.util.List;

/**
 * Everything the bot knows this think pass, frozen.
 *
 * <p>Built once per pass by {@code BotWorldViewFactory} and read many times while rules are
 * matched, so every condition in one rule sees exactly the same world. Nothing in here references
 * a live game object.
 *
 * @param botSide       the side the bot plays
 * @param enemySide     the opposing side
 * @param tier          the acting bot's tier, so a tactic can be gated to stronger opponents
 * @param selfPosition  the position the bot casts from and defends
 * @param mana          mana available right now
 * @param hand          cards in hand
 * @param objects       every observed object, both sides
 * @param enemies       targetable objects owned by {@code enemySide}
 * @param allies        targetable objects owned by {@code botSide}
 * @param enemyClusters groups of enemy mobs close enough for one area spell to cover
 * @param threats       the existing threat model, reused for pressure and contested lane
 * @param enemyPlayerHp remaining hit points of the enemy player
 */
public record BotWorldView(
        Master botSide,
        Master enemySide,
        BotTier tier,
        Vector3 selfPosition,
        int mana,
        List<CardType> hand,
        List<ObservedObject> objects,
        List<ObservedObject> enemies,
        List<ObservedObject> allies,
        List<ObjectCluster> enemyClusters,
        ThreatAssessment threats,
        int enemyPlayerHp
) {

    /** How hard the bot is being pushed, 0 to 1. Delegates to the existing threat model. */
    public double pressure() {
        return threats.pressure();
    }

    public boolean hasEnemies() {
        return !enemies.isEmpty();
    }
}
