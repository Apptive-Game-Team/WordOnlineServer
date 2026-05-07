package com.wordonline.server.game.domain.magic.implement.drop;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("leafair")
public class LeafairMagic extends AbstractDropMagic {

    public LeafairMagic() {
        super(PrefabType.Leafair);
    }
}
