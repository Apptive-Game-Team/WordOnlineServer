package com.wordonline.server.game.domain.object.prefab.implement.wind;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.Spawner;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabProvider;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("wind_summon_prefab")
public class WindSummonPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public WindSummonPrefabInitializer(Parameters parameters) {
        super(PrefabType.WindSummon);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("build", "radius"), true));
        gameObject.setElement(ElementType.WIND);
        gameObject.getComponents().add(new Spawner(gameObject, (int) parameters.getValue("build", "hp"), PrefabType.WindSlime));
    }
}
