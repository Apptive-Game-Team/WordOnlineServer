package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("storm_rider")
public class StormRiderMagic extends AbstractSingleSpawnMagic {
    public StormRiderMagic() {
        super(PrefabType.StormRider);
    }
}
