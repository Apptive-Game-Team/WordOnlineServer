package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import org.springframework.stereotype.Component;

@Component("thunder_spirit")
public class ThunderSpiritMagic extends AbstractSpawnMagic {
    public ThunderSpiritMagic(Parameters parameters) {
        super(PrefabType.ThunderSpirit, parameters.object(GameObjectKey.THUNDER_SPIRIT));
    }
}
