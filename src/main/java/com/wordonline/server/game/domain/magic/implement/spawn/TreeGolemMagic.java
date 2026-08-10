package com.wordonline.server.game.domain.magic.implement.spawn;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;

@Component("tree_golem")
public class TreeGolemMagic extends AbstractSpawnMagic {

    public TreeGolemMagic(Parameters parameters) {
        super(PrefabType.TreeGolem, parameters.object(GameObjectKey.TREE_GOLEM));
    }
}

