package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import org.springframework.stereotype.Component;

@Component("magma_spirit")
public class MagmaSpiritMagic extends AbstractSpawnMagic {
    public MagmaSpiritMagic(Parameters parameters) {
        super(PrefabType.MagmaSpirit, parameters.object(GameObjectKey.MAGMA_SPIRIT));
    }
}
