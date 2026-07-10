package com.wordonline.server.game.domain.object.prefab.implement.nature;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;

@Component("seed_spirit_prefab")
public class SeedSpiritPrefabInitializer extends LeafSlimePrefabInitializer {

    public SeedSpiritPrefabInitializer(Parameters parameters) {
        super(parameters);
    }

    @Override
    protected GameObjectKey getGameObjectKey() {
        return GameObjectKey.SEED_SPIRIT;
    }
}
