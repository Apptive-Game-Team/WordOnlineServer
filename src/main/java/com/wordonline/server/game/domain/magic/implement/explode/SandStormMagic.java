package com.wordonline.server.game.domain.magic.implement.explode;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("sand_storm")
public class SandStormMagic extends AbstractExplosionMagic {
    public SandStormMagic() {
        super(PrefabType.SandStorm);
    }
}