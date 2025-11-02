package com.wordonline.server.game.domain.magic.implement.build.rune;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.magic.implement.build.AbstractSummonMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("nature_rune")
public class NatureRuneMagic extends AbstractSummonMagic {
    public NatureRuneMagic() {
        super(PrefabType.NatureRune);
    }
}