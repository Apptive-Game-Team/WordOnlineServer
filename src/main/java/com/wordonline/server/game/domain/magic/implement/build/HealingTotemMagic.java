package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("healing_totem")
public class HealingTotemMagic extends AbstractSummonMagic {
    public HealingTotemMagic() {
        super(PrefabType.HealingTotem);
    }
}
