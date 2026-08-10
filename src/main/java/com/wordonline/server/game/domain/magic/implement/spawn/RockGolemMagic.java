package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import org.springframework.stereotype.Component;

@Component("rock_golem")
public class RockGolemMagic extends AbstractSpawnMagic {
    public RockGolemMagic(Parameters parameters) {
        super(PrefabType.RockGolem, parameters.object(GameObjectKey.ROCK_GOLEM));
    }
}
