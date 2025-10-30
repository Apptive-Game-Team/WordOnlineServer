package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.ProjectileRangeAttackMob;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.RangeAttackMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

import org.springframework.stereotype.Component;

@Component("aqua_archer_prefab")
public class AquaArcherPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public AquaArcherPrefabInitializer(Parameters parameters) {
        super(PrefabType.AquaArcher);
        this.parameters = parameters;
    }


    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("aqua_archer", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("aqua_archer", "radius"), false));
        gameObject.getComponents().add(new ProjectileRangeAttackMob(gameObject,
                (int) parameters.getValue("aqua_archer", "hp"),
                (float) parameters.getValue("aqua_archer", "speed"),
                TargetMask.ANY.bit,
                (int) parameters.getValue("aqua_archer", "damage"),
                (float) parameters.getValue("aqua_archer", "attack_interval"),
                (float) parameters.getValue("aqua_archer", "attack_range"),
                "WaterShot",
                0.5f
        ));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}
