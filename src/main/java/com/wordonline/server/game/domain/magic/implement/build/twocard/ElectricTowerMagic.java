package com.wordonline.server.game.domain.magic.implement.build.twocard;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.magic.implement.build.AbstractSummonMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("electric_tower")
public class ElectricTowerMagic extends AbstractSummonMagic {
    public ElectricTowerMagic() {
        super(PrefabType.ElectricTower);
    }
}
