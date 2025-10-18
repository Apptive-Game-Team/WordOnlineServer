package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("lightning_slime_nest")
public class LightningSlimeNestMagic extends AbstractSummonMagic {
    public LightningSlimeNestMagic() {
        super(PrefabType.ElectricSummon);
    }
}
