package com.wordonline.server.game.domain.magic.implement.drop;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("fire_drop")
public class FireDropMagic extends AbstractDropMagic {

    public FireDropMagic() {
        super(PrefabType.FireDrop);
    }
}

