package com.wordonline.server.game.domain.object.prefab.implement.misc;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.simple.SummonMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("vine_colony_prefab")
public class VineColonyPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public VineColonyPrefabInitializer(Parameters parameters) {
        super(PrefabType.VineColony);
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
