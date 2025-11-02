package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("cannon")
public class CannonMagic extends AbstractSummonMagic {
    public CannonMagic() {
        super(PrefabType.GroundCannon);
    }
}