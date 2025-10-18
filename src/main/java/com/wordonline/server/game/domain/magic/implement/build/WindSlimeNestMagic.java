package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("wind_slime_nest")
public class WindSlimeNestMagic extends AbstractSummonMagic {
    public WindSlimeNestMagic() {
        super(PrefabType.WindSummon);
    }
}
