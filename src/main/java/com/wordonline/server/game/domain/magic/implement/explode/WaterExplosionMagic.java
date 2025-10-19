package com.wordonline.server.game.domain.magic.implement.explode;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("water_explosion")
public class WaterExplosionMagic extends AbstractExplosionMagic {
    public WaterExplosionMagic() {
        super(PrefabType.WaterExplode);
    }
}