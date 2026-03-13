package com.wordonline.server.game.domain.object.prefab.implement.pve;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.Spawner;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("pve_nature_slime_nest_prefab")
public class PveNatureSlimeNestPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public PveNatureSlimeNestPrefabInitializer(Parameters parameters) {
        super(PrefabType.PveNatureSlimeNest);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("pve_nature_slime_nest", "mass")));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("pve_nature_slime_nest", "radius"), true));
        gameObject.setElement(ElementType.NATURE);
        gameObject.getComponents().add(new Spawner(gameObject, (int) parameters.getValue("pve_nature_slime_nest", "hp"), PrefabType.LeafSlime));
    }
}
