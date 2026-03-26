package com.wordonline.server.game.domain.object.prefab.implement.fire;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;

@Component("ember_spirit_prefab")
public class EmberSpiritPrefabInitializer extends FireSlimePrefabInitializer {

    public EmberSpiritPrefabInitializer(Parameters parameters) {
        super(parameters);
    }
}
