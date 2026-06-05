package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
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
        var aquaArcherParameters = parameters.object(GameObjectKey.AQUA_ARCHER);
        gameObject.getComponents().add(new RigidBody(gameObject, aquaArcherParameters.intValue(ParameterKey.MASS)));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.addCollider(new CircleCollider(gameObject, aquaArcherParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.getComponents().add(new ProjectileRangeAttackMob(gameObject,
                aquaArcherParameters.intValue(ParameterKey.HP),
                aquaArcherParameters.floatValue(ParameterKey.SPEED),
                TargetMask.ANY.bit,
                aquaArcherParameters.intValue(ParameterKey.DAMAGE),
                aquaArcherParameters.floatValue(ParameterKey.ATTACK_INTERVAL),
                aquaArcherParameters.floatValue(ParameterKey.ATTACK_RANGE),
                "WaterShot",
                0.5f
        ));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}
