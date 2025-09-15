package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.magic.Explode;
import com.wordonline.server.game.domain.object.component.magic.Shot;
import com.wordonline.server.game.domain.object.component.physic.Collidable;
import com.wordonline.server.game.dto.Effect;

public class KnockbackEffectProvider extends EffectProvider {

    private Vector2 GetDirection(GameObject otherObject)
    {
        if(gameObject.getComponent(Shot.class) != null)
        {
            return gameObject.getComponent(Shot.class).getDirection();
        }
        else if(gameObject.getComponent(Explode.class) != null)
        {
            return otherObject.getPosition().subtract(this.gameObject.getPosition()).normalize().toVector2();
        }

        return Vector2.ZERO;
    }
    @Override
    public void onCollision(GameObject otherObject) {
        EffectReceiver effectReceiver = (EffectReceiver) otherObject.getComponent(EffectReceiver.class);
        if (effectReceiver != null) {
            effectReceiver.onReceive(effect, GetDirection(otherObject));
        }
    }

    public KnockbackEffectProvider(GameObject gameObject, Effect effect) {
        super(gameObject, effect);
    }
}
