package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.CowardMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("lightning_tadpole_prefab")
public class LightningTadpolePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public LightningTadpolePrefabInitializer(Parameters parameters) {
        super(PrefabType.LightningTadpole);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("lightning_tadpole", "mass")));
        gameObject.addComponent(new ZPhysics(gameObject));
        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("lightning_tadpole", "radius"), false));
        gameObject.addComponent(new CowardMob(gameObject,
                (int) parameters.getValue("lightning_tadpole", "hp"),
                (float) parameters.getValue("lightning_tadpole", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("lightning_tadpole", "damage"),
                (float) parameters.getValue("lightning_tadpole", "attack_interval"),
                (float) parameters.getValue("lightning_tadpole", "detection_range"),
                (float) parameters.getValue("lightning_tadpole", "panic_duration")));
        gameObject.setElement(ElementType.LIGHTNING);
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
