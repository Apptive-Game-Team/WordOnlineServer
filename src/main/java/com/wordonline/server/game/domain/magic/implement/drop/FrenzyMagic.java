package com.wordonline.server.game.domain.magic.implement.drop;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("frenzy_totem")
public class FrenzyMagic extends AbstractDropMagic {
    public FrenzyMagic() {
        super(PrefabType.FrenzyTotem);
    }
}
