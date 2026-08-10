package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import org.springframework.stereotype.Component;

@Component("rock_mage")
public class RockMageMagic extends AbstractSpawnMagic {
    public RockMageMagic(Parameters parameters) {
        super(PrefabType.RockMage, parameters.object(GameObjectKey.ROCK_MAGE));
    }
}
