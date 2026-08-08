package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import org.springframework.stereotype.Component;

@Component("dimension_toad")
public class DimensionToadMagic extends AbstractSpawnMagic {
    public DimensionToadMagic(Parameters parameters) {
        super(PrefabType.DimensionToad, parameters.object(GameObjectKey.DIMENSION_TOAD));
    }
}
