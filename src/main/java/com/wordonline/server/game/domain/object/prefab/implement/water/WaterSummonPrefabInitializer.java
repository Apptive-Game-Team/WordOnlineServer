package com.wordonline.server.game.domain.object.prefab.implement.water;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.Spawner;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabProvider;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("water_summon_prefab")
public class WaterSummonPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;
    private final PrefabInitializer prefabInitializer;

    public WaterSummonPrefabInitializer(Parameters parameters, PrefabProvider prefabProvider) {
        super(PrefabType.WaterSummon);
        this.parameters = parameters;
        this.prefabInitializer = prefabProvider.get(PrefabType.WaterSlime);
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("build", "radius"), true));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new Spawner(gameObject, (int) parameters.getValue("build", "hp"), prefabInitializer));
    }
}
