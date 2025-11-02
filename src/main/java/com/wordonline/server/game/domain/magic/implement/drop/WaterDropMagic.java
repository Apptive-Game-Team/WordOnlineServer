package com.wordonline.server.game.domain.magic.implement.drop;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("water_drop")
public class WaterDropMagic extends AbstractDropMagic {

    public WaterDropMagic() {
        super(PrefabType.WaterDrop);
    }
}

