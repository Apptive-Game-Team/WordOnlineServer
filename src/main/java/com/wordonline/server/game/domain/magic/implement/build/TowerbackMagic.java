package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("towerback")
public class TowerbackMagic extends AbstractSummonMagic {
    public TowerbackMagic() {
        super(PrefabType.Towerback);
    }
}
