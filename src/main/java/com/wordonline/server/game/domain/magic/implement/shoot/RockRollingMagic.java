package com.wordonline.server.game.domain.magic.implement.shoot;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("rock_rolling")
public class RockRollingMagic extends AbstractShotMagic {
    public RockRollingMagic() {
        super(PrefabType.RockRolling);
    }
}