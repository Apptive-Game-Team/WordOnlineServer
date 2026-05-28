package com.wordonline.server.game.domain.magic.implement.explode;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("shock_overload")
public class ShockOverloadMagic extends AbstractExplosionMagic {
    public ShockOverloadMagic() {
        super(PrefabType.ShockOverload);
    }
}
