package com.wordonline.server.game.domain.magic.implement.drop;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("nature_drop")
public class NatureDropMagic extends AbstractDropMagic {

    public NatureDropMagic() {
        super(PrefabType.NatureDrop);
    }
}


