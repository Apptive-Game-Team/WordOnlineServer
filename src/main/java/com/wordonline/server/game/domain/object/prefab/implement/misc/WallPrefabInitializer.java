package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
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
        gameObject.getColliders().add(new EdgeCollider(gameObject, new Vector2(0, 0), new Vector2(0, GameConfig.HEIGHT), false));
        gameObject.getColliders().add(new EdgeCollider(gameObject, new Vector2(GameConfig.WIDTH, GameConfig.HEIGHT), new Vector2(0, GameConfig.HEIGHT), false));
        gameObject.getColliders().add(new EdgeCollider(gameObject, new Vector2(GameConfig.WIDTH, GameConfig.HEIGHT), new Vector2(GameConfig.WIDTH, 0), false));
        gameObject.getColliders().add(new EdgeCollider(gameObject, new Vector2(0, 0), new Vector2(GameConfig.WIDTH, 0), false));
        gameObject.setElement(ElementType.NONE);
    }
}