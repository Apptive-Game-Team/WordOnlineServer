package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.magic.Explode;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("sand_storm_prefab")
public class SandStormPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public SandStormPrefabInitializer(Parameters parameters) {
        super(PrefabType.SandStorm);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var sandStormParameters = parameters.object(GameObjectKey.SAND_STORM);
        gameObject.addCollider(new CircleCollider(gameObject, sandStormParameters.floatValue(ParameterKey.RADIUS), true));
        gameObject.setElement(EnumSet.of(ElementType.ROCK,ElementType.WIND));
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Sandstorm));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, sandStormParameters.floatValue(ParameterKey.DURATION)));
    }
}