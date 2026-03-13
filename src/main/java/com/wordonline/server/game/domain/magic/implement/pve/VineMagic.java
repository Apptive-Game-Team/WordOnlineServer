package com.wordonline.server.game.domain.magic.implement.pve;

import com.wordonline.server.game.domain.magic.implement.build.AbstractSummonMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("vine")
public class VineMagic extends AbstractSummonMagic {

    public VineMagic() {
        super(PrefabType.Vine);
    }
}
