package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.MeleeAttackMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("fire_tadpole_prefab")
public class FireTadpolePrefabInitializer extends PrefabInitializer {

    private static final float TIME_TO_LIVE_SEC = 10f;

    private final Parameters parameters;

    public FireTadpolePrefabInitializer(Parameters parameters) {
        super(PrefabType.FireTadpole);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("fire_tadpole", "mass")));
        gameObject.addComponent(new ZPhysics(gameObject));
        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("fire_tadpole", "radius"), false));
        gameObject.addComponent(new MeleeAttackMob(gameObject,
                (int) parameters.getValue("fire_tadpole", "hp"),
                (float) parameters.getValue("fire_tadpole", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("fire_tadpole", "damage"),
                (float) parameters.getValue("fire_tadpole", "attack_interval")));
        gameObject.addComponent(new TimedSelfDestroyer(gameObject, TIME_TO_LIVE_SEC));
        gameObject.setElement(ElementType.FIRE);
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
