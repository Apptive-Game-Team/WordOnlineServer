package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("magma_spirit")
public class MagmaSpiritMagic extends AbstractSingleSpawnMagic {
    public MagmaSpiritMagic() {
        super(PrefabType.MagmaSpirit);
    }
}
