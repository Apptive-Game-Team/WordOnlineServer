package com.wordonline.server.game.domain.object.prefab.implement.pve;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.magic.Spawner;
import com.wordonline.server.game.domain.object.component.mob.simple.SummonMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("pve_water_slime_nest_prefab")
public class PveVineColonyPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public PveVineColonyPrefabInitializer(Parameters parameters) {
        super(PrefabType.PveWaterSlimeNest);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("vine_colony", "mass")));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("vine_colony", "radius"), false));
        gameObject.getComponents().add(
                new SummonMob(
                        gameObject,
                        (int) parameters.getValue("vine_colony", "hp"),
                        (int) parameters.getValue("vine_colony", "damage"),
                        (int) parameters.getValue("vine_colony", "attack_interval"),
                        (int) parameters.getValue("vine_colony", "attack_range"),
                        PrefabType.Vine
                ));
        gameObject.setElement(ElementType.NATURE);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}
