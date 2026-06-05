package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.PlayerPrioMob;
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
        var stormRiderParameters = parameters.object(GameObjectKey.STORM_RIDER);
        gameObject.getComponents().add(new RigidBody(gameObject, stormRiderParameters.intValue(ParameterKey.MASS)));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.addCollider(new CircleCollider(gameObject, stormRiderParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.getComponents().add(new PlayerPrioMob(gameObject,
                stormRiderParameters.intValue(ParameterKey.HP),
                stormRiderParameters.floatValue(ParameterKey.SPEED),
                TargetMask.GROUND.bit,
                stormRiderParameters.intValue(ParameterKey.DAMAGE),
                stormRiderParameters.floatValue(ParameterKey.ATTACK_INTERVAL)
        ));
        gameObject.setElement(EnumSet.of(ElementType.LIGHTNING,ElementType.WATER));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}
