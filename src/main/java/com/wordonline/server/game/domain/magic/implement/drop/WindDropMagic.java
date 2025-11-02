package com.wordonline.server.game.domain.magic.implement.drop;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("wind_drop")
public class WindDropMagic extends AbstractDropMagic {

    public WindDropMagic() {
        super(PrefabType.WindDrop);
    }
}
