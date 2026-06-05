package com.wordonline.server.game.domain.object.prefab.implement.misc.third;

import java.util.EnumSet;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.AreaEffectProvider;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.ProjectileRangeAttackMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;

@Component("cloud_dragon_prefab")
public class CloudDragonPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public CloudDragonPrefabInitializer(Parameters parameters) {
        super(PrefabType.CloudDragon);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var cloudDragonParameters = parameters.object(GameObjectKey.CLOUD_DRAGON);
        gameObject.addComponent(new RigidBody(gameObject, cloudDragonParameters.intValue(ParameterKey.MASS)));
        gameObject.addComponent(new ZPhysics(gameObject, GameConfig.AERIAL_MOB_INIT_HEIGHT));
        gameObject.addCollider(new CircleCollider(gameObject, cloudDragonParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.addComponent(new ProjectileRangeAttackMob(gameObject,
                cloudDragonParameters.intValue(ParameterKey.HP),
                cloudDragonParameters.floatValue(ParameterKey.SPEED),
                TargetMask.ANY.bit,
                cloudDragonParameters.intValue(ParameterKey.DAMAGE),
                cloudDragonParameters.floatValue(ParameterKey.ATTACK_INTERVAL),
                cloudDragonParameters.floatValue(ParameterKey.ATTACK_RANGE),
                "WaterShot",
                0.5f
        ));
        gameObject.setElement(EnumSet.of(ElementType.WATER, ElementType.WIND));
        gameObject.addComponent(new CommonEffectReceiver(gameObject));

//        gameObject.addCollider(new CircleCollider(gameObject, cloudDragonParameters.floatValue(ParameterKey.ATTACK_RANGE), true));
        gameObject.addComponent(new AreaEffectProvider(gameObject, 1, cloudDragonParameters.floatValue(ParameterKey.ATTACK_RANGE), Effect.Wet));
    }
}
