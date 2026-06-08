package com.wordonline.server.game.domain.object.prefab.implement.fire;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;

@Component("ember_spirit_prefab")
public class EmberSpiritPrefabInitializer extends FireSlimePrefabInitializer {

    public EmberSpiritPrefabInitializer(Parameters parameters) {
        super(parameters);
    }

    @Override
    protected GameObjectKey getGameObjectKey() {
        return GameObjectKey.EMBER_SPIRIT;
    }
}
