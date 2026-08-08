package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import org.springframework.stereotype.Component;

@Component("storm_rider")
public class StormRiderMagic extends AbstractSpawnMagic {
    public StormRiderMagic(Parameters parameters) {
        super(PrefabType.StormRider, parameters.object(GameObjectKey.STORM_RIDER));
    }
}
