package com.wordonline.server.game.domain.object.prefab.implement.water;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.PathSpawner;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.Slime;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("water_slime_prefab")
public class WaterSlimePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public WaterSlimePrefabInitializer(Parameters parameters) {
        super(PrefabType.WaterSlime);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var slimeParameters = parameters.object(GameObjectKey.SLIME);
        gameObject.getComponents().add(new RigidBody(gameObject, slimeParameters.intValue(ParameterKey.MASS)));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.addCollider(new CircleCollider(gameObject, slimeParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.getComponents().add(new Slime(gameObject,
                slimeParameters.intValue(ParameterKey.HP),
                slimeParameters.floatValue(ParameterKey.SPEED),
                TargetMask.GROUND.bit,
                slimeParameters.intValue(ParameterKey.DAMAGE),
                slimeParameters.floatValue(ParameterKey.ATTACK_INTERVAL)));
        gameObject.getComponents().add(new PathSpawner(gameObject, PrefabType.WaterField, 1f));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}