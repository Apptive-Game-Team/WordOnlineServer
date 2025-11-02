package com.wordonline.server.game.domain.magic.implement.drop;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("rock_drop")
public class RockDropMagic extends AbstractDropMagic {

    public RockDropMagic() {
        super(PrefabType.RockDrop);
    }
}
