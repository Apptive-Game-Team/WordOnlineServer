package com.wordonline.server.game.domain.magic.implement.shoot;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import org.springframework.stereotype.Component;

@Component("wind_blade")
public class WindBladeMagic extends AbstractShotMagic {
    public WindBladeMagic() {
        super(PrefabType.WindBlade);
    }

    @Override
    protected Master getMaster(Master master) {
        return master;
    }
}
