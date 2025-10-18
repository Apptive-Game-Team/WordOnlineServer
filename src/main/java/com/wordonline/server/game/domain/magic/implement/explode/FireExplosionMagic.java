package com.wordonline.server.game.domain.magic.implement.explode;

import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("fire_explosion")
public class FireExplosionMagic extends AbstractExplosionMagic {
    public FireExplosionMagic() {
        super(PrefabType.FireExplode);
    }
}
