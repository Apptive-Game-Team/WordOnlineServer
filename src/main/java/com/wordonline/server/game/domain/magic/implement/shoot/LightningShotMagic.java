package com.wordonline.server.game.domain.magic.implement.shoot;

import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("lightning_shot")
public class LightningShotMagic extends AbstractShotMagic {
    public LightningShotMagic() {
        super(PrefabType.ElectricShot);
    }
}
