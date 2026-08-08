package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import org.springframework.stereotype.Component;

@Component("zap_mouse")
public class ZapMouseMagic extends AbstractSpawnMagic {
    public ZapMouseMagic(Parameters parameters) {
        super(PrefabType.ZapMouse, parameters.object(GameObjectKey.ZAP_MOUSE));
    }
}
