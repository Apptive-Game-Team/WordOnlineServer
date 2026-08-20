package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;

final class BotBrainTestFixtures {

    private BotBrainTestFixtures() {
    }

    /** One enemy body on the field, so the brain has something to score a matchup against. */
    static BotVisibleObject enemyUnit() {
        return new BotVisibleObject(1, Master.RightPlayer, PrefabType.FireSpirit, new Vector3(0, 0, 0), 10, true);
    }
}
