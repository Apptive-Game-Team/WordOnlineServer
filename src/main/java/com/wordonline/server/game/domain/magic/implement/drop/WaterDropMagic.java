package com.wordonline.server.game.domain.magic.implement.drop;

import com.wordonline.server.game.domain.magic.implement.explode.AbstractExplosionMagic;
import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("water_drop")
public class WaterDropMagic extends AbstractExplosionMagic {

    public WaterDropMagic() {
        super(PrefabType.WaterDrop);
    }
}

