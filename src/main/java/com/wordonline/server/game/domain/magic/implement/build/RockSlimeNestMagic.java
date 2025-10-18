package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("rock_slime_nest")
public class RockSlimeNestMagic extends AbstractSummonMagic {
    public RockSlimeNestMagic() {
        super(PrefabType.RockSummon);
    }
}
