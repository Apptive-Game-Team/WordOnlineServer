package com.wordonline.server.game.domain.object.prefab.implement.nature;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;

@Component("seed_spirit_prefab")
public class SeedSpiritPrefabInitializer extends LeafSlimePrefabInitializer {

    public SeedSpiritPrefabInitializer(Parameters parameters) {
        super(parameters);
    }
}
