package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("tower")
public class TowerMagic extends AbstractSummonMagic {
    public TowerMagic() {
        super(PrefabType.GroundTower);
    }
}
