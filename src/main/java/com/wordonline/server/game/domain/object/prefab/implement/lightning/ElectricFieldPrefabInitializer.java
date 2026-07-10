package com.wordonline.server.game.domain.object.prefab.implement.lightning;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.object.prefab.implement.field.AbstractFieldPrefabInitializer;
import com.wordonline.server.game.dto.Effect;
import org.springframework.stereotype.Component;

@Component("electric_field_prefab")
public class ElectricFieldPrefabInitializer extends AbstractFieldPrefabInitializer {

    public ElectricFieldPrefabInitializer(Parameters parameters) {
        super(PrefabType.ElectricField, ElementType.LIGHTNING, Effect.Shock, GameObjectKey.ELECTRIC_FIELD, parameters);
    }
}
