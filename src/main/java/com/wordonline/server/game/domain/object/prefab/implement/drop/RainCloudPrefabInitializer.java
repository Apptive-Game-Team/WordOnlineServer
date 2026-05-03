package com.wordonline.server.game.domain.object.prefab.implement.drop;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
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
        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("sand_storm", "radius"), true));
        gameObject.setElement(ElementType.WATER);
        gameObject.addComponent(new EffectProvider(gameObject, Effect.Wet));
        gameObject.addComponent(new EffectProvider(gameObject, Effect.Sandstorm));
        gameObject.addComponent(new TimedSelfDestroyer(gameObject, (float) parameters.getValue("sand_storm", "duration")));
    }
}
