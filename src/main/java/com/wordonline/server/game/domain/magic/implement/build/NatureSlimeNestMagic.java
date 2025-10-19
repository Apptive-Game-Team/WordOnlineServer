package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("nature_slime_nest")
public class NatureSlimeNestMagic extends AbstractSummonMagic {
    public NatureSlimeNestMagic() {
        super(PrefabType.LeafSummon);
    }
}
