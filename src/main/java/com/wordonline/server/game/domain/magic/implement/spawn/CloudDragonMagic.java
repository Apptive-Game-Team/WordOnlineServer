package com.wordonline.server.game.domain.magic.implement.spawn;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("cloud_dragon")
public class CloudDragonMagic extends AbstractSingleSpawnMagic {
    
    public CloudDragonMagic() {
        super(PrefabType.CloudDragon);
    }
}
