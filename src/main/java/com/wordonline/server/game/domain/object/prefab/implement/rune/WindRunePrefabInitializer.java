package com.wordonline.server.game.domain.object.prefab.implement.rune;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;

@Component("wind_rune_prefab")
public class WindRunePrefabInitializer extends AbstractRunePrefabInitializer {

    public WindRunePrefabInitializer(Parameters parameters) {
        super(parameters, ElementType.WIND);
    }
}

