package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.object.Vector3;

/**
 * A snapshot of one hostile object as the bot sees it during a single think pass.
 *
 * @param objectId                   id of the observed game object, for logging only
 * @param position                   position at observation time
 * @param hp                         remaining hit points, used to avoid overkill
 * @param distanceToDefendedPosition distance from the position the bot is defending
 * @param playerCore                 true when this is the enemy player itself, the win condition
 */
public record EnemyThreat(
        int objectId,
        Vector3 position,
        int hp,
        double distanceToDefendedPosition,
        boolean playerCore
) {
}
