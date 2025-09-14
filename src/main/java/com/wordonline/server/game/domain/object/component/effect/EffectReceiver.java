package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.physic.Collidable;
import com.wordonline.server.game.dto.Effect;

public interface EffectReceiver extends Collidable {
    void onReceive(Effect effect);
    void onReceive(Effect effect, Vector2 direction);

    @Override
    default void onCollision(GameObject otherObject) {}
}
