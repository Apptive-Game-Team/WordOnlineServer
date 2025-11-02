package com.wordonline.server.game.domain.object.prefab.implement.drop;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;

@Component("water_drop_prefab")
public class WaterDropPrefabInitializer extends AbstractDropPrefabInitializer {

    public WaterDropPrefabInitializer(Parameters parameters) {
        super(ElementType.WATER, parameters);
    }
}
