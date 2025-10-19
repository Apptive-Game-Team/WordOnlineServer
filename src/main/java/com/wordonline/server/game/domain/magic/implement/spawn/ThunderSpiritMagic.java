package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("thunder_spirit")
public class ThunderSpiritMagic extends AbstractSingleSpawnMagic {
    public ThunderSpiritMagic() {
        super(PrefabType.ThunderSpirit);
    }
}
