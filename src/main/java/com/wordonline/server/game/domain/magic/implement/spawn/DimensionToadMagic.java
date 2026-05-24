package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("dimension_toad")
public class DimensionToadMagic extends AbstractSingleSpawnMagic {
    public DimensionToadMagic() {
        super(PrefabType.DimensionToad);
    }
}
