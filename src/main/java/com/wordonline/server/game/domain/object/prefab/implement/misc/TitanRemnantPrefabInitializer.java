package com.wordonline.server.game.domain.object.prefab.implement.misc;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.simple.SummonMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;

@Component("titan_remnant_prefab")
public class TitanRemnantPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public TitanRemnantPrefabInitializer(Parameters parameters) {
        super(PrefabType.TitanRemnant);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var remnantParameters = parameters.object(GameObjectKey.TITAN_REMNANT);
        gameObject.addComponent(new RigidBody(
                gameObject,
                remnantParameters.intValue(ParameterKey.MASS)
        ));
        gameObject.addCollider(new CircleCollider(
                gameObject,
                remnantParameters.floatValue(ParameterKey.RADIUS),
                false
        ));
        gameObject.addComponent(new SummonMob(
                gameObject,
                remnantParameters.intValue(ParameterKey.HP),
                0f,
                remnantParameters.floatValue(ParameterKey.ATTACK_INTERVAL),
                remnantParameters.floatValue(ParameterKey.ATTACK_RANGE),
                PrefabType.TitanFist
        ));
        gameObject.addComponent(new TimedSelfDestroyer(
                gameObject,
                remnantParameters.floatValue(ParameterKey.DURATION)
        ));
        gameObject.setElement(ElementType.ROCK);
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
