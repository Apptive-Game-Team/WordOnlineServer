package com.wordonline.server.game.domain.object.prefab.implement.lightning;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import org.springframework.stereotype.Component;

@Component("electric_field_prefab")
public class ElectricFieldPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public ElectricFieldPrefabInitializer(Parameters parameters) {
        super(PrefabType.ElectricField);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("field_short", "radius"), true));
        gameObject.setElement(ElementType.LIGHTNING);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, (float) parameters.getValue("field_short", "duration")));
    }
}