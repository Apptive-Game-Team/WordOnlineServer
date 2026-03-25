package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("seed_spirit_swarm")
public class SeedSpiritSwarmMagic extends AbstractSwarmSpawnMagic {
    public SeedSpiritSwarmMagic() {
        super(PrefabType.SeedSpirit);
    }
}
