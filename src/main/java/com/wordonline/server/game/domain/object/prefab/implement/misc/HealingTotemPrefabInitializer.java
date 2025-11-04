package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.Cannon;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.MeleeAttackMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("healing_totem_prefab")
public class HealingTotemPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public HealingTotemPrefabInitializer(Parameters parameters) {
        super(PrefabType.HealingTotem);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("healing_totem", "mass")));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("healing_totem", "radius"), false));
        gameObject.getComponents().add(new Cannon(gameObject, (int) parameters.getValue("healing_totem", "hp"), (int) parameters.getValue("ground_cannon", "damage"), TargetMask.GROUND.bit));
        gameObject.setElement(ElementType.NATURE);
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}