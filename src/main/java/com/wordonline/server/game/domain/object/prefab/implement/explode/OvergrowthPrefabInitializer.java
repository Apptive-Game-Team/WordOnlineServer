package com.wordonline.server.game.domain.object.prefab.implement.explode;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.OvergrowthExplosion;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("overgrowth_prefab")
public class OvergrowthPrefabInitializer extends PrefabInitializer {
    private final Parameters parameters;

    public OvergrowthPrefabInitializer(Parameters parameters) {
        super(PrefabType.Overgrowth);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        float radius = (float) parameters.getValue("overgrowth", "radius");
        int summonCount = (int) parameters.getValue("overgrowth", "quantity");

        gameObject.addCollider(new CircleCollider(gameObject, radius, true));
        gameObject.setElement(ElementType.NATURE);
        gameObject.addComponent(new OvergrowthExplosion(gameObject, radius, summonCount));
    }
}
