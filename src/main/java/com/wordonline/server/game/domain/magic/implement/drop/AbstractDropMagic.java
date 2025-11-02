package com.wordonline.server.game.domain.magic.implement.drop;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.magic.Drop;
import com.wordonline.server.game.domain.object.component.magic.Shot;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;

public abstract class AbstractDropMagic extends Magic {

    private static final float DROP_MAGIC_INITIAL_Z_POSITION = 10;

    private final PrefabType prefabType;

    public AbstractDropMagic(PrefabType prefabType) {
        super(CardType.Drop);
        this.prefabType = prefabType;
    }

    @Override
    public void run(GameContext gameContext, Master master, Vector3 position) {
        position.setZ(DROP_MAGIC_INITIAL_Z_POSITION);
        GameObject gameObject = new GameObject(
                master,
                prefabType,
                position,
                gameContext);
        gameObject.getComponent(Drop.class);
    }
}

