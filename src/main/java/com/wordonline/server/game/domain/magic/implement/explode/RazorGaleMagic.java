package com.wordonline.server.game.domain.magic.implement.explode;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("razor_gale")
public class RazorGaleMagic extends AbstractExplosionMagic {
    public RazorGaleMagic() {
        super(PrefabType.RazorGale);
    }
}
