package com.wordonline.server.game.domain.magic.implement.explode;

import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("rock_explosion")
public class RockExplosionMagic extends AbstractExplosionMagic {
    public RockExplosionMagic() {
        super(PrefabType.RockExplode);
    }
}
