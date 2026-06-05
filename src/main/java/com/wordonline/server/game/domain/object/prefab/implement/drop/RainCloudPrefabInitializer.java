package com.wordonline.server.game.domain.object.prefab.implement.drop;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;

@Component("rain_cloud_prefab")
public class RainCloudPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public RainCloudPrefabInitializer(Parameters parameters) {
        super(PrefabType.RainCloud);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var sandStormParameters = parameters.object(GameObjectKey.SAND_STORM);
        gameObject.addCollider(new CircleCollider(gameObject, sandStormParameters.floatValue(ParameterKey.RADIUS), true));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Wet));
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Sandstorm));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, sandStormParameters.floatValue(ParameterKey.DURATION)));
    }
}
