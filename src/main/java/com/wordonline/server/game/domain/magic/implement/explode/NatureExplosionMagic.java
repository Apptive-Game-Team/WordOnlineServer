package com.wordonline.server.game.domain.magic.implement.explode;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("nature_explosion")
public class NatureExplosionMagic extends AbstractExplosionMagic {
    public NatureExplosionMagic() {
        super(PrefabType.LeafExplode);
    }
}