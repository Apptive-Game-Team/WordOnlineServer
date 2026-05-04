package com.wordonline.server.game.domain.object.prefab.implement.explode;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.OvergrowthExplosion;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("overgrowth_prefab")
public class OvergrowthPrefabInitializer extends PrefabInitializer {
    private static final float RADIUS = 3f;

    public OvergrowthPrefabInitializer() {
        super(PrefabType.Overgrowth);
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addCollider(new CircleCollider(gameObject, RADIUS, true));
        gameObject.setElement(ElementType.NATURE);
        gameObject.addComponent(new OvergrowthExplosion(gameObject));
    }
}
