package com.wordonline.server.game.domain.object.prefab.implement.drop;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;

@Component("nature_drop_prefab")
public class NatureDropPrefabInitializer extends AbstractDropPrefabInitializer {

    public NatureDropPrefabInitializer(Parameters parameters) {
        super(ElementType.NATURE, parameters);
    }
}
