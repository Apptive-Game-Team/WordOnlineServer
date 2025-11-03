package com.wordonline.server.game.domain.object.prefab.implement.rune;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;

@Component("nature_rune_prefab")
public class NatureRunePrefabInitializer extends AbstractRunePrefabInitializer {

    public NatureRunePrefabInitializer(Parameters parameters) {
        super(parameters, ElementType.NATURE);
    }
}
