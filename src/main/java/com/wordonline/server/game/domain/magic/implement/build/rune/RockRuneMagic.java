package com.wordonline.server.game.domain.magic.implement.build.rune;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.magic.implement.build.AbstractSummonMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("rock_rune")
public class RockRuneMagic extends AbstractSummonMagic {
    public RockRuneMagic() {
        super(PrefabType.RockRune);
    }
}