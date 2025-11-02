package com.wordonline.server.game.domain.object.prefab.implement.drop;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;

@Component("lightning_drop_prefab")
public class LightningDropPrefabInitializer extends AbstractDropPrefabInitializer {

    public LightningDropPrefabInitializer(Parameters parameters) {
        super(ElementType.LIGHTNING, parameters);
    }
}
