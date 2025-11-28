package com.wordonline.server.game.domain.magic.implement.build;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("vine_colony")
public class VineColonyMagic extends AbstractSummonMagic {

    public VineColonyMagic() {
        super(PrefabType.VineColony);
    }
}
