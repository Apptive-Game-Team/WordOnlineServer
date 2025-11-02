package com.wordonline.server.game.domain.magic.implement.build.rune;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.magic.implement.build.AbstractSummonMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("lightning_rune")
public class LightningRuneMagic extends AbstractSummonMagic {
    public LightningRuneMagic() {
        super(PrefabType.LightningRune);
    }
}
