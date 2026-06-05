package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.MeleeAttackMob;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.SummonerMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("magma_spirit_prefab")
public class MagmaSpiritPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public MagmaSpiritPrefabInitializer(Parameters parameters) {
        super(PrefabType.MagmaSpirit);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var fireSpiritParameters = parameters.object(GameObjectKey.FIRE_SPIRIT);
        var magmaSpiritParameters = parameters.object(GameObjectKey.MAGMA_SPIRIT);
        gameObject.getComponents().add(new RigidBody(gameObject, fireSpiritParameters.intValue(ParameterKey.MASS)));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.addCollider(new CircleCollider(gameObject, fireSpiritParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.getComponents().add(new SummonerMob(gameObject,
                magmaSpiritParameters.intValue(ParameterKey.HP),
                magmaSpiritParameters.floatValue(ParameterKey.SPEED),
                TargetMask.GROUND.bit,
                magmaSpiritParameters.floatValue(ParameterKey.ATTACK_INTERVAL),
                magmaSpiritParameters.floatValue(ParameterKey.ATTACK_RANGE),
                PrefabType.MagmaFist
        ));
        gameObject.setElement(EnumSet.of(ElementType.FIRE,ElementType.ROCK));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}