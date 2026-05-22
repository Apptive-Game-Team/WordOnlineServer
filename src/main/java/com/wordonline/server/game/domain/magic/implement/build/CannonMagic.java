package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.springframework.stereotype.Component;

@Component("cannon")
public class CannonMagic extends AbstractSummonMagic {
    public CannonMagic() {
        super(PrefabType.GroundCannon);
    }

    @Override
    public void run(GameContext gameContext, Master master, Vector3 position) {
        Vector3 groundedPosition = new Vector3(position.getX(), position.getY(), 0f);
        new GameObject(getMaster(master), PrefabType.GroundCannon, groundedPosition, gameContext);
    }
}
