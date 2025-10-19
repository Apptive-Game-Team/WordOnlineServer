package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("water_slime_swarm")
public class WaterSlimeSwarmMagic extends AbstractSwarmSpawnMagic {
    public WaterSlimeSwarmMagic() {
        super(PrefabType.WaterSlime);
    }
}
