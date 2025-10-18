package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("water_slime_nest")
public class WaterSlimeNestMagic extends AbstractSummonMagic {
    public WaterSlimeNestMagic() {
        super(PrefabType.WaterSummon);
    }
}
