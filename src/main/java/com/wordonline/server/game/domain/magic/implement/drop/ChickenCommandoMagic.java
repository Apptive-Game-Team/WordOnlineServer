package com.wordonline.server.game.domain.magic.implement.drop;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.springframework.stereotype.Component;

@Component("chicken_commando")
public class ChickenCommandoMagic extends Magic {

    private static final float SPAWN_HEIGHT = 4f;

    public ChickenCommandoMagic() {
        super(CardType.Drop);
    }

    @Override
    public void run(GameContext gameContext, Master master, Vector3 position) {
        Vector3 spawnPosition = new Vector3(position.getX(), position.getY(), SPAWN_HEIGHT);
        new GameObject(master, PrefabType.ChickenCommando, spawnPosition, gameContext);
    }
}
