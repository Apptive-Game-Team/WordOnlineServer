package com.wordonline.server.game.domain.magic.implement.build.rune;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.magic.implement.build.AbstractSummonMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("fire_rune")
public class FireRuneMagic extends AbstractSummonMagic {
    public FireRuneMagic() {
        super(PrefabType.FireRune);
    }
}








