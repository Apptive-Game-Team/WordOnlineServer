package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.magic.implement.build.AbstractSummonMagic;
import com.wordonline.server.game.domain.object.PrefabType;
import org.springframework.stereotype.Component;

@Component("thunder_spirit")
public class ThunderSpiritMagic extends AbstractSummonMagic {
    public ThunderSpiritMagic() {
        super(PrefabType.ThunderSpirit);
    }
}
