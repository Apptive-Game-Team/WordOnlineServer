package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("fire_slime_nest")
public class FireSlimeNestMagic extends AbstractSummonMagic {
    public FireSlimeNestMagic() {
        super(PrefabType.FireSummon);
    }
}
