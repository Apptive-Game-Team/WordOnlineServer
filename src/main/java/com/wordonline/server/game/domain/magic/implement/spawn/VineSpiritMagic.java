package com.wordonline.server.game.domain.magic.implement.spawn;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("vine_spirit")
public class VineSpiritMagic extends AbstractSingleSpawnMagic {

    public VineSpiritMagic() {
        super(PrefabType.VineSpirit);
    }
}
