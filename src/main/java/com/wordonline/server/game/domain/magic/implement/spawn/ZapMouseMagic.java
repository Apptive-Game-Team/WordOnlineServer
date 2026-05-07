package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("zap_mouse")
public class ZapMouseMagic extends AbstractSingleSpawnMagic {
    public ZapMouseMagic() {
        super(PrefabType.ZapMouse);
    }
}
