package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import org.springframework.stereotype.Component;

@Component("water_slime_swarm")
public class WaterSlimeSwarmMagic extends AbstractSwarmSpawnMagic {

    // Fixed swarm size; the shared WATER_SLIME quantity parameter still drives the PVE nests.
    private static final int SWARM_SIZE = 3;

    public WaterSlimeSwarmMagic(Parameters parameters) {
        super(PrefabType.WaterSlime, parameters.object(GameObjectKey.WATER_SLIME));
    }

    @Override
    protected int getNum() {
        return SWARM_SIZE;
    }
}
