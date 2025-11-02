package com.wordonline.server.game.domain.object.prefab.implement.drop;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;

@Component("wind_drop_prefab")
public class WindDropPrefabInitializer extends AbstractDropPrefabInitializer {

    public WindDropPrefabInitializer(Parameters parameters) {
        super(ElementType.WIND, parameters);
    }
}
