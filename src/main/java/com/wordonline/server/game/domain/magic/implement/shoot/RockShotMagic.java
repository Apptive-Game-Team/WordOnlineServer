package com.wordonline.server.game.domain.magic.implement.shoot;

import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("rock_shot")
public class RockShotMagic extends AbstractShotMagic {
    public RockShotMagic() {
        super(PrefabType.RockShot);
    }
}
