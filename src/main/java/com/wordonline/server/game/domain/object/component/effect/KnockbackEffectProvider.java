package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.effect.receiver.EffectReceiver;
import com.wordonline.server.game.domain.object.component.magic.Explode;
import com.wordonline.server.game.domain.object.component.magic.Shot;
import com.wordonline.server.game.dto.Effect;

public class KnockbackEffectProvider extends EffectProvider {

    private Vector3 GetDirection(GameObject otherObject)
    {
        if(gameObject.getComponent(Shot.class) != null)
        {
            return gameObject.getComponent(Shot.class).getDirection();
        }
        else if(gameObject.getComponent(Explode.class) != null)
        {
            return otherObject.getPosition().subtract(this.gameObject.getPosition()).normalize();
        }

        return Vector3.ZERO;
    }

    private float GetProximity(GameObject otherObject)
    {
        Parameters parameters = getGameContext().getParameters();
        if(gameObject.getComponent(Shot.class) != null)
        {
            var shootParameters = parameters.object(GameObjectKey.SHOOT);
            return (float) (gameObject.getPosition().distance(otherObject.getPosition().toVector2()) / shootParameters.doubleValue(ParameterKey.RADIUS));
        }
        else if(gameObject.getComponent(Explode.class) != null)
        {
            var explodeParameters = parameters.object(GameObjectKey.EXPLODE);
            return (float) (gameObject.getPosition().distance(otherObject.getPosition().toVector2()) / explodeParameters.doubleValue(ParameterKey.RADIUS));
        }

        return 0;
    }

    @Override
    public void onCollision(GameObject otherObject) {
        EffectReceiver effectReceiver = (EffectReceiver) otherObject.getComponent(EffectReceiver.class);
        if (effectReceiver != null) {
            effectReceiver.onReceive(effect, GetDirection(otherObject),GetProximity(otherObject));
        }
    }

    public KnockbackEffectProvider(GameObject gameObject, Effect effect) {
        super(gameObject, effect);
    }
}
