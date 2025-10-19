package com.wordonline.server.game.domain.object.prefab.implement.nature;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.Spawner;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabProvider;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("leaf_summon_prefab")
public class LeafSummonPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public LeafSummonPrefabInitializer(Parameters parameters) {
        super(PrefabType.LeafSummon);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("build", "radius"), true));
        gameObject.setElement(ElementType.NATURE);
        gameObject.getComponents().add(new Spawner(gameObject, (int) parameters.getValue("build", "hp"), PrefabType.LeafSlime));
    }
}
