package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("wind_spirit")
public class WindSpiritMagic extends AbstractSingleSpawnMagic {
    public WindSpiritMagic() {
        super(PrefabType.WindSlime);
    }
}
