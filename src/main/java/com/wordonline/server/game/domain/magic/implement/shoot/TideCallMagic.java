package com.wordonline.server.game.domain.magic.implement.shoot;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("tide_call")
public class TideCallMagic extends AbstractShotMagic {
    public TideCallMagic() {
        super(PrefabType.TideCall);
    }
}