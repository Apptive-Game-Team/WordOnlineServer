package com.wordonline.server.game.domain.magic.implement.explode;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("magma_explosion")
public class MagmaExplosionMagic extends AbstractExplosionMagic {
    public MagmaExplosionMagic() {
        super(PrefabType.MagmaExplosion);
    }
}
