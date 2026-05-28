package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("bubble_spirit")
public class BubbleSpiritMagic extends AbstractSingleSpawnMagic {
    public BubbleSpiritMagic() {
        super(PrefabType.BubbleSpirit);
    }
}
