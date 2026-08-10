package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import org.springframework.stereotype.Component;

@Component("seed_spirit_swarm")
public class SeedSpiritSwarmMagic extends AbstractSpawnMagic {
    public SeedSpiritSwarmMagic(Parameters parameters) {
        super(PrefabType.SeedSpirit, parameters.object(GameObjectKey.SEED_SPIRIT));
    }
}
