package com.wordonline.server.game.domain.magic.implement.build.twocard;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.magic.implement.build.AbstractSummonMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("crater")
public class CraterMagic extends AbstractSummonMagic {
    public CraterMagic() {
        super(PrefabType.Crater);
    }
}
