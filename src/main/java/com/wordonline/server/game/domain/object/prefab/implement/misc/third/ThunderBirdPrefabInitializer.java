package com.wordonline.server.game.domain.object.prefab.implement.misc.third;

import java.util.EnumSet;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.LightningSummonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.ThunderBirdMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("thunder_bird_prefab")
public class ThunderBirdPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public ThunderBirdPrefabInitializer(Parameters parameters) {
        super(PrefabType.ThunderBird);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var thunderBirdParameters = parameters.object(GameObjectKey.THUNDER_BIRD);
        gameObject.getComponents().add(new RigidBody(gameObject, thunderBirdParameters.intValue(ParameterKey.MASS)));
        gameObject.getComponents().add(new ZPhysics(gameObject, GameConfig.AERIAL_MOB_INIT_HEIGHT));
        gameObject.addCollider(new CircleCollider(gameObject, thunderBirdParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.getComponents().add(new ThunderBirdMob(gameObject,
                thunderBirdParameters.intValue(ParameterKey.HP),
                thunderBirdParameters.floatValue(ParameterKey.SPEED),
                TargetMask.GROUND.bit,
                thunderBirdParameters.intValue(ParameterKey.DAMAGE),
                thunderBirdParameters.floatValue(ParameterKey.ATTACK_INTERVAL),
                thunderBirdParameters.floatValue(ParameterKey.ATTACK_RANGE)
        ));
        gameObject.setElement(EnumSet.of(ElementType.LIGHTNING));
        gameObject.getComponents().add(new LightningSummonEffectReceiver(gameObject));
    }
}
