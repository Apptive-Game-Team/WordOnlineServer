package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("fire_spirit")
public class FireSpiritMagic extends AbstractSingleSpawnMagic {
    public FireSpiritMagic() {
        super(PrefabType.FireSpirit);
    }
}
