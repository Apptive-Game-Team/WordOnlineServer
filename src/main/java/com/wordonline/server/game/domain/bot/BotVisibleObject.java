package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;

// One game object as the bot saw it at a single frame. Everything the brain reads off an object is
// copied in here, position included: Vector3 is mutable and the loop thread moves the live one every
// frame, so holding a reference to it would leak the race this type exists to close.
//
// hp and targetable are copied for the same reason: threat assessment used to read the mob component
// and the status flags from the bot executor thread, while the loop thread could be flushing them.
public record BotVisibleObject(
        int id,
        Master master,
        PrefabType type,
        Vector3 position,
        int hp,
        boolean mob,
        boolean targetable
) {

    /** Assumed hit points when the object carries no mob component. */
    static final int UNKNOWN_HP = 10;

    // Called on the loop thread while nothing else is mutating the object.
    public static BotVisibleObject of(GameObject gameObject) {
        return new BotVisibleObject(
                gameObject.getId(),
                gameObject.getMaster(),
                gameObject.getType(),
                new Vector3(gameObject.getPosition()),
                readHp(gameObject),
                gameObject.hasComponent(Mob.class),
                gameObject.isActive() && !gameObject.isDying()
        );
    }

    private static int readHp(GameObject gameObject) {
        Mob mob = gameObject.getComponent(Mob.class);
        return mob == null ? UNKNOWN_HP : Math.max(1, mob.getHp());
    }
}
