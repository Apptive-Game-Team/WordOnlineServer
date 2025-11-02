package com.wordonline.server.game.domain.magic.implement.build.rune;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.magic.implement.build.AbstractSummonMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("wind_rune")
public class WindRuneMagic extends AbstractSummonMagic {
    public WindRuneMagic() {
        super(PrefabType.WindRune);
    }
}