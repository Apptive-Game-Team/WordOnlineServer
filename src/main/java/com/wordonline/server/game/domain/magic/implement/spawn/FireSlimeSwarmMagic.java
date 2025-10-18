package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("fire_slime_swarm")
public class FireSlimeSwarmMagic extends AbstractSwarmSpawnMagic {
    public FireSlimeSwarmMagic() {
        super(PrefabType.FireSlime);
    }
}
