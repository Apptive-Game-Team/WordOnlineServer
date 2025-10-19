package com.wordonline.server.game.domain.object.prefab.implement.lightning;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.Spawner;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabProvider;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("electric_summon_prefab")
public class ElectricSummonPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;
    private final PrefabInitializer prefabInitializer;

    public ElectricSummonPrefabInitializer(Parameters parameters, PrefabProvider prefabProvider) {
        super(PrefabType.ElectricSummon);
        this.parameters = parameters;
        this.prefabInitializer = prefabProvider.get(PrefabType.ElectricSlime);
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("build", "radius"), true));
        gameObject.setElement(ElementType.LIGHTNING);
        gameObject.getComponents().add(new Spawner(gameObject, (int) parameters.getValue("build", "hp"), prefabInitializer));
    }
}
