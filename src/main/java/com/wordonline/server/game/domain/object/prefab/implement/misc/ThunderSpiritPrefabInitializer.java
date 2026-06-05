package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.ProjectileRangeAttackMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("thunder_spirit_prefab")
public class ThunderSpiritPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public ThunderSpiritPrefabInitializer(Parameters parameters) {
        super(PrefabType.ThunderSpirit);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var thunderSpiritParameters = parameters.object(GameObjectKey.THUNDER_SPIRIT);
        gameObject.getComponents().add(new RigidBody(gameObject, thunderSpiritParameters.intValue(ParameterKey.MASS)));
        gameObject.getComponents().add(new ZPhysics(gameObject, GameConfig.AERIAL_MOB_INIT_HEIGHT));
        gameObject.addCollider(new CircleCollider(gameObject, thunderSpiritParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.getComponents().add(new ProjectileRangeAttackMob(gameObject,
                thunderSpiritParameters.intValue(ParameterKey.HP),
                thunderSpiritParameters.floatValue(ParameterKey.SPEED),
                TargetMask.GROUND.bit,
                thunderSpiritParameters.intValue(ParameterKey.DAMAGE),
                thunderSpiritParameters.floatValue(ParameterKey.ATTACK_INTERVAL),
                thunderSpiritParameters.floatValue(ParameterKey.ATTACK_RANGE),
                "ElectricShot",
                0.5f
        ));
        gameObject.setElement(EnumSet.of(ElementType.LIGHTNING,ElementType.WIND));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}