package com.wordonline.server.game.domain.magic.implement.build.twocard;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.magic.implement.build.AbstractSummonMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("rock_turret")
public class RockTurretMagic extends AbstractSummonMagic {
    public RockTurretMagic() {
        super(PrefabType.RockTurret);
    }
}