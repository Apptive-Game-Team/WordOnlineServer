package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.magic.implement.explode.AbstractExplosionMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("tornado_strike")
public class TornadoStrikeMagic extends AbstractExplosionMagic {
    public TornadoStrikeMagic() {
        super(PrefabType.TornadoStrike);
    }
}