package com.wordonline.server.game.domain.object.prefab.implement.drop;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;

@Component("fire_drop_prefab")
public class FireDropPrefabInitializer extends AbstractDropPrefabInitializer {

    public FireDropPrefabInitializer(Parameters parameters) {
        super(ElementType.FIRE, parameters);
    }
}

