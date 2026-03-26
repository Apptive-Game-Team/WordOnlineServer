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

@Component("water_drop_prefab")
public class WaterDropPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public WaterDropPrefabInitializer(Parameters parameters) {
        super(PrefabType.WaterDrop);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("sand_storm", "radius"), true));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Wet));
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Sandstorm));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, (float) parameters.getValue("sand_storm", "duration")));
    }
}
