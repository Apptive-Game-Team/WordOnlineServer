package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import org.springframework.stereotype.Component;

@Component("aqua_archer")
public class AquaArcherMagic extends AbstractSpawnMagic {
    public AquaArcherMagic(Parameters parameters) {
        super(PrefabType.AquaArcher, parameters.object(GameObjectKey.AQUA_ARCHER));
    }
}
