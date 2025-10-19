package com.wordonline.server.game.domain.magic.implement.shoot;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import org.springframework.stereotype.Component;

@Component("wind_shot")
public class WindShotMagic extends AbstractShotMagic {
    public WindShotMagic() {
        super(PrefabType.WindShot);
    }

    @Override
    protected Master getMaster(Master master) {
        return Master.None;
    }
}