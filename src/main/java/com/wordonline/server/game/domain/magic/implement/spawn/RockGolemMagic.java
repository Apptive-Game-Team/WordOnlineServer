package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.magic.implement.build.AbstractSummonMagic;
import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("rock_golem")
public class RockGolemMagic extends AbstractSummonMagic {
    public RockGolemMagic() {
        super(PrefabType.RockGolem);
    }
}
