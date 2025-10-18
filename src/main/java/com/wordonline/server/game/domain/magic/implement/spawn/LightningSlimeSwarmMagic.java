package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("lightning_slime_swarm")
public class LightningSlimeSwarmMagic extends AbstractSpawnMagic {
    public LightningSlimeSwarmMagic() {
        super(PrefabType.ElectricSlime);
    }
}
