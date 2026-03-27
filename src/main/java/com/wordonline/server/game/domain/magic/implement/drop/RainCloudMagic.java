package com.wordonline.server.game.domain.magic.implement.drop;

import com.wordonline.server.game.domain.magic.implement.explode.AbstractExplosionMagic;
import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("rain_cloud")
public class RainCloudMagic extends AbstractExplosionMagic {

    public RainCloudMagic() {
        super(PrefabType.RainCloud);
    }
}

