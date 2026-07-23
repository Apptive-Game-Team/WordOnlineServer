package com.wordonline.server.game.domain.object.prefab.implement.fire;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.object.prefab.implement.field.AbstractFieldPrefabInitializer;
import com.wordonline.server.game.dto.Effect;
import org.springframework.stereotype.Component;

@Component("fire_field_prefab")
public class FireFieldPrefabInitializer extends AbstractFieldPrefabInitializer {

    public FireFieldPrefabInitializer(Parameters parameters) {
        super(PrefabType.FireField, ElementType.FIRE, Effect.Burn, GameObjectKey.FIRE_FIELD, parameters);
    }
}
