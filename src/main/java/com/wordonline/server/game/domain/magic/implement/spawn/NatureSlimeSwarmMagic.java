package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("nature_slime_swarm")
public class NatureSlimeSwarmMagic extends AbstractSwarmSpawnMagic {
    public NatureSlimeSwarmMagic() {
        super(PrefabType.LeafSlime);
    }
}
