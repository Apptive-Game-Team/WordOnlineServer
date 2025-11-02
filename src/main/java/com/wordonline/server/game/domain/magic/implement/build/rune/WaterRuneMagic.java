package com.wordonline.server.game.domain.magic.implement.build.rune;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.magic.implement.build.AbstractSummonMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("water_rune")
public class WaterRuneMagic extends AbstractSummonMagic {
    public WaterRuneMagic() {
        super(PrefabType.WaterRune);
    }
}
