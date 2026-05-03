package com.wordonline.server.game.domain.object.prefab.implement.rock;

import org.springframework.stereotype.Component;

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

@Component("mini_rock_prefab")
public class MiniRockPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public MiniRockPrefabInitializer(Parameters parameters) {
        super(PrefabType.RockSlime);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("mini_rock", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("mini_rock", "radius"), false));
        gameObject.getComponents().add(new Slime(gameObject,
                (int) parameters.getValue("mini_rock", "hp"),
                (float) parameters.getValue("mini_rock", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("mini_rock", "damage"),
                (float) parameters.getValue("mini_rock", "attack_interval")));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}