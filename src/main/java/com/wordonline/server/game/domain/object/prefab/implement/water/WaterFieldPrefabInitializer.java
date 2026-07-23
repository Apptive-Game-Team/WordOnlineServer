package com.wordonline.server.game.domain.object.prefab.implement.water;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.WaterFieldEffectReceiver;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.object.prefab.implement.field.AbstractFieldPrefabInitializer;
import com.wordonline.server.game.dto.Effect;
import org.springframework.stereotype.Component;

@Component("water_field_prefab")
public class WaterFieldPrefabInitializer extends AbstractFieldPrefabInitializer {

    public WaterFieldPrefabInitializer(Parameters parameters) {
        super(PrefabType.WaterField, ElementType.WATER, Effect.Wet, GameObjectKey.WATER_FIELD, parameters);
    }

    @Override
    protected void addExtraComponents(GameObject gameObject, Parameters parameters) {
        gameObject.getComponents().add(new WaterFieldEffectReceiver(gameObject));
    }
}
