package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("ember_spirit_swarm")
public class EmberSpiritSwarmMagic extends AbstractSwarmSpawnMagic {
    public EmberSpiritSwarmMagic() {
        super(PrefabType.EmberSpirit);
    }
}
