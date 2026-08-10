package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import org.springframework.stereotype.Component;

@Component("fire_lord_spirit")
public class FireLordSpiritMagic extends AbstractSpawnMagic {
    public FireLordSpiritMagic(Parameters parameters) {
        super(PrefabType.FireLordSpirit, parameters.object(GameObjectKey.FIRE_LORD_SPIRIT));
    }
}
