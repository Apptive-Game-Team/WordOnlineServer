package com.wordonline.server.game.domain.magic.implement.build.twocard;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.magic.implement.build.AbstractSummonMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("bubble_generator")
public class BubbleGeneratorMagic extends AbstractSummonMagic {
    public BubbleGeneratorMagic() {
        super(PrefabType.BubbleGenerator);
    }
}
