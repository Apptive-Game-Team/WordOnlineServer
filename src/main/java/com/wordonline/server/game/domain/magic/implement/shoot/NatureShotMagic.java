package com.wordonline.server.game.domain.magic.implement.shoot;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("nature_shot")
public class NatureShotMagic extends AbstractShotMagic {
    public NatureShotMagic() {
        super(PrefabType.LeafShot);
    }
}