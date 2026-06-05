package com.wordonline.server.game.domain.object.prefab.implement.misc.third;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.EffectProvideProjectileRangeAttackMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;

@Component("vine_spirit_prefab")
public class VineSpiritPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public VineSpiritPrefabInitializer(Parameters parameters) {
        super(PrefabType.VineSpirit);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var vineSpiritParameters = parameters.object(GameObjectKey.VINE_SPIRIT);
        gameObject.getComponents().add(new RigidBody(gameObject, vineSpiritParameters.intValue(ParameterKey.MASS)));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.addCollider(new CircleCollider(gameObject, vineSpiritParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.getComponents().add(new EffectProvideProjectileRangeAttackMob(gameObject,
                vineSpiritParameters.intValue(ParameterKey.HP),
                vineSpiritParameters.floatValue(ParameterKey.SPEED),
                TargetMask.ANY.bit,
                vineSpiritParameters.intValue(ParameterKey.DAMAGE),
                vineSpiritParameters.floatValue(ParameterKey.ATTACK_INTERVAL),
                vineSpiritParameters.floatValue(ParameterKey.ATTACK_RANGE),
                Effect.Snared,
                "NatureShot",
                0.5f
        ));
        gameObject.setElement(ElementType.NATURE);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}
