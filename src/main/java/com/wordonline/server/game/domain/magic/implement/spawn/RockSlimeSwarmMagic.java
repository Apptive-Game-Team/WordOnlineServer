package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("rock_slime_swarm")
public class RockSlimeSwarmMagic extends AbstractSwarmSpawnMagic {
    public RockSlimeSwarmMagic() {
        super(PrefabType.RockSlime);
    }
}
