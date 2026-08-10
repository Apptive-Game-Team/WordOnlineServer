package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("rallying_totem")
public class RallyingTotemMagic extends AbstractSummonMagic {
    public RallyingTotemMagic() {
        super(PrefabType.RallyingTotem);
    }
}
