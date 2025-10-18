package com.wordonline.server.game.domain.magic.implement.explode;

import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("lightning_explosion")
public class LightningExplosionMagic extends AbstractExplosionMagic {
    public LightningExplosionMagic() {
        super(PrefabType.ElectricExplode);
    }
}
