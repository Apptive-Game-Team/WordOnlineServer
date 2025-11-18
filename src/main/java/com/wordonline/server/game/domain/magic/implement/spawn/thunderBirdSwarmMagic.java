package com.wordonline.server.game.domain.magic.implement.spawn;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("thunder_bird_swarm")
public class thunderBirdSwarmMagic extends AbstractSwarmSpawnMagic {

    public thunderBirdSwarmMagic() {
        super(PrefabType.ThunderBird);
    }
}
