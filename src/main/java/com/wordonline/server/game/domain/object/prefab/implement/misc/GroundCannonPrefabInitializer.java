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

@Component("ground_cannon_prefab")
public class GroundCannonPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public GroundCannonPrefabInitializer(Parameters parameters) {
        super(PrefabType.GroundCannon);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("ground_cannon", "mass")));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("ground_cannon", "radius"), false));
        gameObject.getComponents().add(new Cannon(gameObject, (int) parameters.getValue("ground_cannon", "hp"), (int) parameters.getValue("ground_cannon", "damage"), TargetMask.GROUND.bit));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}