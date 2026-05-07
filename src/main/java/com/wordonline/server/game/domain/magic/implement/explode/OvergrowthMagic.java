package com.wordonline.server.game.domain.magic.implement.explode;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("overgrowth")
public class OvergrowthMagic extends AbstractExplosionMagic {
    public OvergrowthMagic() {
        super(PrefabType.Overgrowth);
    }
}
