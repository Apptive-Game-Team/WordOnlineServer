package com.wordonline.server.game.domain.object.prefab.implement.lightning;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.magic.Explode;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import org.springframework.stereotype.Component;

@Component("electric_explode_prefab")
public class ElectricExplodePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public ElectricExplodePrefabInitializer(Parameters parameters) {
        super(PrefabType.ElectricExplode);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("explode", "radius"), true));
        gameObject.setElement(ElementType.LIGHTNING);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new Explode(gameObject, (int) parameters.getValue("explode", "damage")));
    }
}