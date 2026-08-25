package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.bot.view.BotWorldView;
import com.wordonline.server.game.domain.bot.view.ObjectCluster;
import com.wordonline.server.game.domain.bot.view.ObservedObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;

/**
 * One place a rule wants to put a spell, and what made it that place.
 *
 * <p>The subject is kept alongside the coordinates because scoring usually needs it - a cluster
 * tactic scores on how many bodies the group holds - and because the explanation that ends up on
 * the decision has to name what the bot was shooting at, not just where.
 *
 * @param position    where the spell lands
 * @param object      the object aimed at, or {@code null} when the aim is not about one object
 * @param cluster     the group aimed at, or {@code null} when the aim is not about a group
 * @param description how to say this aim out loud, for the decision's reason string
 */
public record Aim(Vector3 position, ObservedObject object, ObjectCluster cluster, String description) {

    public static Aim at(ObservedObject object, BotWorldView view) {
        return new Aim(new Vector3(object.position()), object, null, describe(object, view));
    }

    public static Aim at(ObjectCluster cluster) {
        return new Aim(
                new Vector3(cluster.center()),
                null,
                cluster,
                "a cluster of " + cluster.size() + " enemy mobs");
    }

    public static Aim at(Vector3 position, String description) {
        return new Aim(new Vector3(position), null, null, description);
    }

    private static String describe(ObservedObject object, BotWorldView view) {
        String side;
        if (object.master() == view.botSide()) {
            side = "allied ";
        } else if (object.master() == view.enemySide() && object.master() != Master.None) {
            side = "enemy ";
        } else {
            side = "neutral ";
        }
        return side + object.type() + " " + object.id();
    }
}
