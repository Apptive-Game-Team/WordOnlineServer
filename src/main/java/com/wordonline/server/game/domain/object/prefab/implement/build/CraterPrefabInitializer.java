package com.wordonline.server.game.domain.object.prefab.implement.build;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.receiver.BuildingEffectReceiver;
import com.wordonline.server.game.domain.object.component.magic.CraterSpawner;
import com.wordonline.server.game.domain.object.component.mob.simple.DummyMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("crater_prefab")
public class CraterPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public CraterPrefabInitializer(Parameters parameters) {
        super(PrefabType.Crater);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var craterParameters = parameters.object(GameObjectKey.CRATER);
        gameObject.addComponent(new RigidBody(gameObject, craterParameters.intValue(ParameterKey.MASS)));
        gameObject.addCollider(new CircleCollider(gameObject, craterParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.addComponent(new DummyMob(gameObject, craterParameters.intValue(ParameterKey.HP)));
        gameObject.addComponent(new CraterSpawner(gameObject, craterParameters.floatValue(ParameterKey.ATTACK_INTERVAL)));
        gameObject.addComponent(new TimedSelfDestroyer(gameObject, craterParameters.floatValue(ParameterKey.DURATION)));
        gameObject.setElement(ElementType.FIRE);
        gameObject.addComponent(new BuildingEffectReceiver(gameObject));
    }
}
