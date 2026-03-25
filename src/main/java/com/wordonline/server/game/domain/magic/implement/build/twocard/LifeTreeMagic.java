package com.wordonline.server.game.domain.magic.implement.build.twocard;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.magic.implement.build.AbstractSummonMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("life_tree")
public class LifeTreeMagic extends AbstractSummonMagic {
    public LifeTreeMagic() {
        super(PrefabType.LifeTree);
    }
}