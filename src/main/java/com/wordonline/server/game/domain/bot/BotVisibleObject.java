package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;

// One game object as the bot saw it at a single frame. Everything the brain reads off an object is
// copied in here, position included: Vector3 is mutable and the loop thread moves the live one every
// frame, so holding a reference to it would leak the race this type exists to close.
public record BotVisibleObject(Master master, PrefabType type, Vector3 position) {

    // Called on the loop thread while nothing else is mutating the object.
    public static BotVisibleObject of(GameObject gameObject) {
        return new BotVisibleObject(
                gameObject.getMaster(),
                gameObject.getType(),
                new Vector3(gameObject.getPosition())
        );
    }
}
