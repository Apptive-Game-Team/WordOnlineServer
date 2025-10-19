package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.Cannon;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("ground_tower_prefab")
public class GroundTowerPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public GroundTowerPrefabInitializer(Parameters parameters) {
        super(PrefabType.GroundTower);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("ground_tower", "mass")));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("ground_tower", "radius"), false));
        gameObject.getComponents().add(new Cannon(gameObject, (int) parameters.getValue("ground_tower", "hp"), (int) parameters.getValue("ground_tower", "damage"), TargetMask.AIR.bit));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}