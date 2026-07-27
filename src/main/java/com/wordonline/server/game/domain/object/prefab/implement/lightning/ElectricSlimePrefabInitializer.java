package com.wordonline.server.game.domain.object.prefab.implement.lightning;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.ElectricDeathEnergy;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.Slime;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("electric_slime_prefab")
public class ElectricSlimePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public ElectricSlimePrefabInitializer(Parameters parameters) {
        super(PrefabType.ElectricSlime);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var slimeParameters = parameters.object(GameObjectKey.ELECTRIC_SLIME);
        gameObject.getComponents().add(new RigidBody(gameObject, slimeParameters.intValue(ParameterKey.MASS)));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.addCollider(new CircleCollider(gameObject, slimeParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.getComponents().add(new Slime(gameObject,
                slimeParameters.intValue(ParameterKey.HP),
                slimeParameters.floatValue(ParameterKey.SPEED),
                TargetMask.GROUND.bit,
                slimeParameters.intValue(ParameterKey.DAMAGE),
                slimeParameters.floatValue(ParameterKey.ATTACK_INTERVAL)));
        gameObject.setElement(ElementType.LIGHTNING);
        gameObject.addComponent(new ElectricDeathEnergy(gameObject));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}
