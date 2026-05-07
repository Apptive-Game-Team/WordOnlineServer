package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.ChickenCommandoMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("chicken_commando_prefab")
public class ChickenCommandoPrefabInitializer extends PrefabInitializer {

    private static final float FALL_GRAVITY = 3f;

    private final Parameters parameters;

    public ChickenCommandoPrefabInitializer(Parameters parameters) {
        super(PrefabType.ChickenCommando);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("chicken_commando", "mass")));

        ZPhysics zPhysics = new ZPhysics(gameObject);
        zPhysics.setGravity(FALL_GRAVITY);
        zPhysics.setFallThreshold(Float.MAX_VALUE);
        gameObject.addComponent(zPhysics);

        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("chicken_commando", "radius"), false));
        gameObject.addComponent(new ChickenCommandoMob(
                gameObject,
                (int) parameters.getValue("chicken_commando", "hp"),
                (float) parameters.getValue("chicken_commando", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("chicken_commando", "damage"),
                (float) parameters.getValue("chicken_commando", "attack_interval")
        ));
        gameObject.setElement(EnumSet.of(ElementType.WIND));
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
