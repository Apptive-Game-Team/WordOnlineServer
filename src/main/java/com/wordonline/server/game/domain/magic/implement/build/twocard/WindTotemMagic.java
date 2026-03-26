package com.wordonline.server.game.domain.magic.implement.build.twocard;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.magic.implement.build.AbstractSummonMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("wind_totem")
public class WindTotemMagic extends AbstractSummonMagic {
    public WindTotemMagic() {
        super(PrefabType.WindTotem);
    }
}