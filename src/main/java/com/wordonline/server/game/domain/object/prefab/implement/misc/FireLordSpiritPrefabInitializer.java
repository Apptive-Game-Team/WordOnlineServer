package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.magic.LimitedSequenceSpawner;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.KeepDistanceMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("fire_lord_spirit_prefab")
public class FireLordSpiritPrefabInitializer extends PrefabInitializer {

    private static final float CHILD_SPAWN_INTERVAL_SEC = 5f;
    private static final int MAX_CHILD_SPAWN_COUNT = 5;
    private static final float KEEP_DISTANCE_RANGE = 4f;

    private final Parameters parameters;

    public FireLordSpiritPrefabInitializer(Parameters parameters) {
        super(PrefabType.FireLordSpirit);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var fireLordSpiritParameters = parameters.object(GameObjectKey.FIRE_LORD_SPIRIT);
        gameObject.addComponent(new RigidBody(gameObject, fireLordSpiritParameters.intValue(ParameterKey.MASS)));
        gameObject.addComponent(new ZPhysics(gameObject, GameConfig.AERIAL_MOB_INIT_HEIGHT));
        gameObject.addCollider(new CircleCollider(gameObject, fireLordSpiritParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.addComponent(new KeepDistanceMob(gameObject,
                fireLordSpiritParameters.intValue(ParameterKey.HP),
                fireLordSpiritParameters.floatValue(ParameterKey.SPEED),
                TargetMask.ANY.bit,
                KEEP_DISTANCE_RANGE));
        gameObject.addComponent(new LimitedSequenceSpawner(
                gameObject,
                CHILD_SPAWN_INTERVAL_SEC,
                MAX_CHILD_SPAWN_COUNT,
                PrefabType.FireChildSpirit));
        gameObject.setElement(EnumSet.of(ElementType.FIRE, ElementType.WIND));
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
