package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import org.springframework.stereotype.Component;

@Component("mini_rock_swarm")
public class MiniRockSwarmMagic extends AbstractSpawnMagic {

    public MiniRockSwarmMagic(Parameters parameters) {
        super(PrefabType.MiniRock, parameters.object(GameObjectKey.MINI_ROCK));
    }
}
