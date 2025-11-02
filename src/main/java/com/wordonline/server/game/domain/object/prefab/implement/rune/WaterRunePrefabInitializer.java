package com.wordonline.server.game.domain.object.prefab.implement.rune;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;

@Component("water_rune_prefab")
public class WaterRunePrefabInitializer extends AbstractRunePrefabInitializer {

    public WaterRunePrefabInitializer(Parameters parameters) {
        super(parameters, ElementType.WATER);
    }
}
