package com.wordonline.server.game.domain.magic.implement.spawn;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("tree_golem")
public class TreeGolemMagic extends AbstractSingleSpawnMagic {

    public TreeGolemMagic() {
        super(PrefabType.TreeGolem);
    }
}

