package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.physic.EdgeCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("wall_prefab")
public class WallPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public WallPrefabInitializer(Parameters parameters) {
        super(PrefabType.Wall);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new EdgeCollider(gameObject, new Vector3(0, 0, 0), new Vector3(0, GameConfig.HEIGHT, 0), false));
        gameObject.getColliders().add(new EdgeCollider(gameObject, new Vector3(GameConfig.WIDTH, GameConfig.HEIGHT, 0), new Vector3(0, GameConfig.HEIGHT, 0), false));
        gameObject.getColliders().add(new EdgeCollider(gameObject, new Vector3(GameConfig.WIDTH, GameConfig.HEIGHT, 0), new Vector3(GameConfig.WIDTH, 0, 0), false));
        gameObject.getColliders().add(new EdgeCollider(gameObject, new Vector3(0, 0, 0), new Vector3(GameConfig.WIDTH, 0, 0), false));
        gameObject.setElement(ElementType.NONE);
    }
}