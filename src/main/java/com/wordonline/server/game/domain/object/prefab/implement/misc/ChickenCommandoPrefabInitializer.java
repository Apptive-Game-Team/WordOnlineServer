package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.ChickenCommandoMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("chicken_commando_prefab")
public class ChickenCommandoPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public ChickenCommandoPrefabInitializer(Parameters parameters) {
        super(PrefabType.ChickenCommando);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var chickenCommandoParameters = parameters.object(GameObjectKey.CHICKEN_COMMANDO);
        gameObject.addComponent(new RigidBody(gameObject, chickenCommandoParameters.intValue(ParameterKey.MASS)));

        ZPhysics zPhysics = new ZPhysics(gameObject);
        zPhysics.setGravity(chickenCommandoParameters.floatValue(ParameterKey.FALL_GRAVITY));
        zPhysics.setFallThreshold(Float.MAX_VALUE);
        gameObject.addComponent(zPhysics);

        gameObject.addCollider(new CircleCollider(gameObject, chickenCommandoParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.addComponent(new ChickenCommandoMob(
                gameObject,
                chickenCommandoParameters.intValue(ParameterKey.HP),
                chickenCommandoParameters.floatValue(ParameterKey.SPEED),
                TargetMask.GROUND.bit,
                chickenCommandoParameters.intValue(ParameterKey.DAMAGE),
                chickenCommandoParameters.floatValue(ParameterKey.ATTACK_INTERVAL)
        ));
        gameObject.setElement(EnumSet.of(ElementType.WIND));
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
