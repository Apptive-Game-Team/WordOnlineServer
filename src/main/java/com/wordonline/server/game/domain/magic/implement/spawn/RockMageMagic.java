package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("rock_mage")
public class RockMageMagic extends AbstractSingleSpawnMagic {
    public RockMageMagic() {
        super(PrefabType.RockMage);
    }
}
