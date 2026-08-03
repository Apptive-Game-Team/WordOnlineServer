package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("seed_nest")
public class SeedNestMagic extends AbstractSummonMagic {

    public SeedNestMagic() {
        super(PrefabType.SeedNest);
    }
}
