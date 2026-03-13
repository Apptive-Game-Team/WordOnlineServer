package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("pve_water_slime_nest")
public class PveWaterSlimeNestMagic extends AbstractSummonMagic {
    public PveWaterSlimeNestMagic() {
        super(PrefabType.PveWaterSlimeNest);
    }
}
