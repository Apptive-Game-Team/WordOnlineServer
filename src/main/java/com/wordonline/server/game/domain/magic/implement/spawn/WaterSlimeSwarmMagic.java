package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import org.springframework.stereotype.Component;

@Component("water_slime_swarm")
public class WaterSlimeSwarmMagic extends AbstractSwarmSpawnMagic {
    public WaterSlimeSwarmMagic(Parameters parameters) {
        super(PrefabType.WaterSlime, parameters.object(GameObjectKey.WATER_SLIME));
    }
}
