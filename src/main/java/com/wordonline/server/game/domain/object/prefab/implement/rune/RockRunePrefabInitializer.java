package com.wordonline.server.game.domain.object.prefab.implement.rune;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;

@Component("rock_rune_prefab")
public class RockRunePrefabInitializer extends AbstractRunePrefabInitializer {

    public RockRunePrefabInitializer(Parameters parameters) {
        super(parameters, ElementType.ROCK);
    }
}
