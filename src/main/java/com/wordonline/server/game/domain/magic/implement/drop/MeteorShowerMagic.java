package com.wordonline.server.game.domain.magic.implement.drop;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("meteor_shower")
public class MeteorShowerMagic extends AbstractDropMagic {

    public MeteorShowerMagic() {
        super(PrefabType.MeteorShower);
    }
}

