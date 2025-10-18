package com.wordonline.server.game.domain.magic.implement.shoot;

import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("water_shot")
public class WaterShotMagic extends AbstractShotMagic {
    public WaterShotMagic() {
        super(PrefabType.WaterShot);
    }
}
