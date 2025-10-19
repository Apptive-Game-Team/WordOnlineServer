package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("mana_well")
public class ManaWellMagic extends AbstractSummonMagic {
    public ManaWellMagic() {
        super(PrefabType.ManaWell);
    }
}
