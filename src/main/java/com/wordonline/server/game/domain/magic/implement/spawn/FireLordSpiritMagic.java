package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("fire_lord_spirit")
public class FireLordSpiritMagic extends AbstractSingleSpawnMagic {
    public FireLordSpiritMagic() {
        super(PrefabType.FireLordSpirit);
    }
}
