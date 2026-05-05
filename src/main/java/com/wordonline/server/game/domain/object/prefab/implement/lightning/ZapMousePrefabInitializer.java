package com.wordonline.server.game.domain.object.prefab.implement.lightning;

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

@Component("zap_mouse_prefab")
public class ZapMousePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public ZapMousePrefabInitializer(Parameters parameters) {
        super(PrefabType.ZapMouse);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("zap_mouse", "mass")));
        gameObject.addComponent(new ZPhysics(gameObject));
        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("zap_mouse", "radius"), false));
        gameObject.addComponent(new Slime(gameObject,
                (int) parameters.getValue("zap_mouse", "hp"),
                (float) parameters.getValue("zap_mouse", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("zap_mouse", "damage"),
                (float) parameters.getValue("zap_mouse", "attack_interval")));
        gameObject.setElement(ElementType.LIGHTNING);
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
