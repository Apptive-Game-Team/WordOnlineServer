package com.wordonline.server.game.domain.magic.implement.drop;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("rain_cloud")
public class RainCloudMagic extends AbstractDropMagic {

    public RainCloudMagic() {
        super(PrefabType.RainCloud);
    }
}
