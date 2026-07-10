package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.PathSpawner;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.magic.PushShot;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import org.springframework.stereotype.Component;

@Component("tide_call_prefab")
public class TideCallPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public TideCallPrefabInitializer(Parameters parameters) {
        super(PrefabType.TideCall);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var tideCallParameters = parameters.object(GameObjectKey.TIDE_CALL);
        gameObject.addCollider(new CircleCollider(gameObject, tideCallParameters.floatValue(ParameterKey.RADIUS), true));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Wet));
        gameObject.getComponents().add(new PushShot(
                gameObject,
                tideCallParameters.intValue(ParameterKey.DAMAGE),
                tideCallParameters.floatValue(ParameterKey.SPEED)
        ));
        gameObject.getComponents().add(new PathSpawner(gameObject, PrefabType.WaterField, 0.5f));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, tideCallParameters.floatValue(ParameterKey.DURATION)));
    }
}
