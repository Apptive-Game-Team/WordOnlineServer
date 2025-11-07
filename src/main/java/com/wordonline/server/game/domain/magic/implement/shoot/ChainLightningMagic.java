package com.wordonline.server.game.domain.magic.implement.shoot;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("chain_lightning")
public class ChainLightningMagic extends AbstractShotMagic {
    public ChainLightningMagic() {
        super(PrefabType.ChainLightning);
    }
}