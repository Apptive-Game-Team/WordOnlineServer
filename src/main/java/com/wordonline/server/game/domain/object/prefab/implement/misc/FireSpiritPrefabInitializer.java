package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.AreaEffectProvider;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.SprayingAttacker;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;

import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("fire_spirit_prefab")
public class FireSpiritPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public FireSpiritPrefabInitializer(Parameters parameters) {
        super(PrefabType.FireSpirit);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var fireSpiritParameters = parameters.object(GameObjectKey.FIRE_SPIRIT);
        gameObject.addComponent(new RigidBody(gameObject, fireSpiritParameters.intValue(ParameterKey.MASS)));
        gameObject.addComponent(new ZPhysics(gameObject));
        gameObject.addCollider(new CircleCollider(gameObject, fireSpiritParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.addComponent(new SprayingAttacker(gameObject,
                fireSpiritParameters.intValue(ParameterKey.HP),
                fireSpiritParameters.floatValue(ParameterKey.SPEED),
                TargetMask.GROUND.bit,
                fireSpiritParameters.floatValue(ParameterKey.ATTACK_INTERVAL),
                fireSpiritParameters.floatValue(ParameterKey.ATTACK_RANGE),
                fireSpiritParameters.intValue(ParameterKey.DAMAGE),
                Effect.Burn,
                "SprayedFlame"
        ));
        gameObject.setElement(EnumSet.of(ElementType.FIRE,ElementType.WIND));
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
        gameObject.addComponent(new AreaEffectProvider(
                gameObject,
                1f,
                fireSpiritParameters.floatValue(ParameterKey.ATTACK_RANGE),
                Effect.Burn
                ));
    }
}