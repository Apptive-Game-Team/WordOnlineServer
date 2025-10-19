package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.MeleeAttackMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("storm_rider_prefab")
public class StormRiderPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public StormRiderPrefabInitializer(Parameters parameters) {
        super(PrefabType.StormRider);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("storm_rider", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("storm_rider", "radius"), false));
        gameObject.getComponents().add(new MeleeAttackMob(gameObject,
                (int) parameters.getValue("storm_rider", "hp"),
                (float) parameters.getValue("storm_rider", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("storm_rider", "damage"),
                (float) parameters.getValue("storm_rider", "attack_interval")
        ));
        gameObject.setElement(EnumSet.of(ElementType.LIGHTNING,ElementType.WATER));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}