package com.wordonline.server.game.domain.magic.implement.spawn;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;

@Component("vine_spirit")
public class VineSpiritMagic extends AbstractSpawnMagic {

    public VineSpiritMagic(Parameters parameters) {
        super(PrefabType.VineSpirit, parameters.object(GameObjectKey.VINE_SPIRIT));
    }
}
