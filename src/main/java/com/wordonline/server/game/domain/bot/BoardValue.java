package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * What a side's board is worth, priced in the mana that was spent to put it there.
 *
 * <p>Counting bodies would call a two-mana critter and a twelve-mana golem the same thing, which is
 * no basis for "summon something weaker than what they have out". Mana is the game's own measure of
 * investment, the bot already reasons in it, and it follows balance changes on its own - unlike a
 * per-unit strength number, which would be one more hand-maintained column to forget about when a
 * unit is added.
 *
 * <p>The price of a unit is the cost of the recipe that summons it, found through the name space
 * that game objects and magics share.
 */
@Slf4j
final class BoardValue {

    /** Charged for a unit whose summoning recipe cannot be resolved. */
    static final int UNKNOWN_COST = 0;

    private final DatabaseMagicParser magicParser;
    private final BotSpellStats spellStats;
    private final Map<PrefabType, Integer> costByPrefab = new HashMap<>();

    BoardValue(DatabaseMagicParser magicParser, BotSpellStats spellStats) {
        this.magicParser = magicParser;
        this.spellStats = spellStats;
    }

    /** Mana the given side has standing on the field. The player core is not something anyone summoned. */
    int manaOnField(List<BotVisibleObject> objects, Master side) {
        int total = 0;
        for (BotVisibleObject object : objects) {
            if (object.master() != side || object.type() == PrefabType.Player || !object.targetable()) {
                continue;
            }
            total += manaCostOf(object.type());
        }
        return total;
    }

    /**
     * Mana the recipe that summons this prefab costs, or {@link #UNKNOWN_COST} when it cannot be
     * resolved. Unknown prices count as nothing rather than as a guess: over-pricing the enemy board
     * would let the hospitality bot summon something stronger than the rule intends.
     */
    int manaCostOf(PrefabType type) {
        Integer cached = costByPrefab.get(type);
        if (cached != null) {
            return cached;
        }

        int cost = resolveCost(type);
        costByPrefab.put(type, cost);
        return cost;
    }

    private int resolveCost(PrefabType type) {
        try {
            Magic magic = magicParser.parseMagicForBot(TagRepository.toGameObjectName(type));
            if (magic == null || magic.id <= 0) {
                return UNKNOWN_COST;
            }

            for (Map.Entry<List<CardType>, Magic> entry : magicParser.getAllMagicRecipeMap().entrySet()) {
                if (entry.getValue().id == magic.id) {
                    return spellStats.totalManaCost(entry.getKey());
                }
            }
        } catch (RuntimeException e) {
            log.debug("[BoardValue] Could not price {}; counting it as free", type, e);
        }
        return UNKNOWN_COST;
    }
}
