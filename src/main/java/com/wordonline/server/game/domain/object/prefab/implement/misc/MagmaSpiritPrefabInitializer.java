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

@Component("magma_spirit_prefab")
public class MagmaSpiritPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public MagmaSpiritPrefabInitializer(Parameters parameters) {
        super(PrefabType.MagmaSpirit);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("magma_spirit", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("magma_spirit", "radius"), false));
        gameObject.getComponents().add(new MeleeAttackMob(gameObject,
                (int) parameters.getValue("magma_spirit", "hp"),
                (float) parameters.getValue("magma_spirit", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("magma_spirit", "damage"),
                (float) parameters.getValue("magma_spirit", "attack_interval")
        ));
        gameObject.setElement(ElementType.FIRE);
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}