package com.wordonline.server.game.domain.object.prefab.implement.rune;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;

@Component("lightning_rune_prefab")
public class LightningRunePrefabInitializer extends AbstractRunePrefabInitializer {

    public LightningRunePrefabInitializer(Parameters parameters) {
        super(parameters, ElementType.LIGHTNING);
    }
}
