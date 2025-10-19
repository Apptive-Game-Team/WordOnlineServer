package com.wordonline.server.game.domain.magic.implement.explode;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import org.springframework.stereotype.Component;

@Component("wind_explosion")
public class WindExplosionMagic extends AbstractExplosionMagic {
    public WindExplosionMagic() {
        super(PrefabType.WindExplode);
    }

    @Override
    protected Master getMaster(Master master) {
        return Master.None;
    }
}