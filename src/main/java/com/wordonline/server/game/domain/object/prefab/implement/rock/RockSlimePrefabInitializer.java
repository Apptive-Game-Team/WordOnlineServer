package com.wordonline.server.game.domain.object.prefab.implement.rock;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.Slime;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("rock_slime_prefab")
public class RockSlimePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public RockSlimePrefabInitializer(Parameters parameters) {
        super(PrefabType.RockSlime);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("slime", "mass")));
        gameObject.addComponent(new ZPhysics(gameObject));
        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("slime", "radius"), false));
        gameObject.addComponent(new Slime(gameObject,
                (int) parameters.getValue("slime", "hp"),
                (float) parameters.getValue("slime", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("slime", "damage"),
                (float) parameters.getValue("slime", "attack_interval")));
        gameObject.setElement(ElementType.ROCK);
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}