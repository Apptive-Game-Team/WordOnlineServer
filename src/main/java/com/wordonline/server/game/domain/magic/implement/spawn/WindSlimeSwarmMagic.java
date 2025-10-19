package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("wind_slime_swarm")
public class WindSlimeSwarmMagic extends AbstractSwarmSpawnMagic {
    public WindSlimeSwarmMagic() {
        super(PrefabType.WindSlime);
    }
}
