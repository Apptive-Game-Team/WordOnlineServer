package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.magic.LimitedSequenceSpawner;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.NonAttackingCowardMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("dimension_toad_prefab")
public class DimensionToadPrefabInitializer extends PrefabInitializer {

    private static final float TADPOLE_SPAWN_INTERVAL_SEC = 10f;
    private static final int INFINITE_TADPOLE_SPAWN_COUNT = 0;
    private static final float KEEP_DISTANCE_RANGE = 4f;

    private final Parameters parameters;

    public DimensionToadPrefabInitializer(Parameters parameters) {
        super(PrefabType.DimensionToad);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var dimensionToadParameters = parameters.object(GameObjectKey.DIMENSION_TOAD);
        gameObject.addComponent(new RigidBody(gameObject, dimensionToadParameters.intValue(ParameterKey.MASS)));
        gameObject.addComponent(new ZPhysics(gameObject));
        gameObject.addCollider(new CircleCollider(gameObject, dimensionToadParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.addComponent(new NonAttackingCowardMob(gameObject,
                dimensionToadParameters.intValue(ParameterKey.HP),
                dimensionToadParameters.floatValue(ParameterKey.SPEED),
                TargetMask.GROUND.bit,
                dimensionToadParameters.floatValue(ParameterKey.ATTACK_INTERVAL),
                dimensionToadParameters.floatValue(ParameterKey.DETECTION_RANGE),
                dimensionToadParameters.floatValue(ParameterKey.PANIC_DURATION),
                KEEP_DISTANCE_RANGE));
        gameObject.addComponent(new LimitedSequenceSpawner(
                gameObject,
                TADPOLE_SPAWN_INTERVAL_SEC,
                INFINITE_TADPOLE_SPAWN_COUNT,
                PrefabType.FireTadpole,
                PrefabType.LightningTadpole));
        gameObject.setElement(EnumSet.of(ElementType.FIRE, ElementType.LIGHTNING));
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
