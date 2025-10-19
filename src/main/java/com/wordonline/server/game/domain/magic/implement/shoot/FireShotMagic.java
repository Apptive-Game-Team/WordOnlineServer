package com.wordonline.server.game.domain.magic.implement.shoot;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("fire_shot")
public class FireShotMagic extends AbstractShotMagic {
    public FireShotMagic() {
        super(PrefabType.FireShot);
    }
}