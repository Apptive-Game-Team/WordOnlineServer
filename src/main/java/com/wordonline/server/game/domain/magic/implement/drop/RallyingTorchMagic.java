package com.wordonline.server.game.domain.magic.implement.drop;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("rallying_torch")
public class RallyingTorchMagic extends AbstractDropMagic {
    public RallyingTorchMagic() {
        super(PrefabType.RallyingTorch);
    }
}
