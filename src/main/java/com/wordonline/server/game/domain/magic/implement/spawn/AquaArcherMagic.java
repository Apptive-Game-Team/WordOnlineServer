package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.magic.implement.build.AbstractSummonMagic;
import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("aqua_archer")
public class AquaArcherMagic extends AbstractSingleSpawnMagic {
    public AquaArcherMagic() {
        super(PrefabType.AquaArcher);
    }
}
