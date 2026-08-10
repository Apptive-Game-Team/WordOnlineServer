package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import org.springframework.stereotype.Component;

@Component("ember_spirit_swarm")
public class EmberSpiritSwarmMagic extends AbstractSpawnMagic {
    public EmberSpiritSwarmMagic(Parameters parameters) {
        super(PrefabType.EmberSpirit, parameters.object(GameObjectKey.EMBER_SPIRIT));
    }
}
