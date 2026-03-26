package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("mini_rock_swarm")
public class MiniRockSwarmMagic extends AbstractSwarmSpawnMagic {

    @Override
    protected int getNum() {
        return 2;
    }

    public MiniRockSwarmMagic() {
        super(PrefabType.MiniRock);
    }
}
