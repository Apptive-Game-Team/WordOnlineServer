package com.wordonline.server.game.domain.object.prefab.implement.build;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.receiver.BuildingEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.simple.BubbleGeneratorMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("bubble_generator_prefab")
public class BubbleGeneratorPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public BubbleGeneratorPrefabInitializer(Parameters parameters) {
        super(PrefabType.BubbleGenerator);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var bubbleGeneratorParameters = parameters.object(GameObjectKey.BUBBLE_GENERATOR);
        var shootParameters = parameters.object(GameObjectKey.SHOOT);
        gameObject.addComponent(new RigidBody(gameObject, bubbleGeneratorParameters.intValue(ParameterKey.MASS)));
        gameObject.addCollider(new CircleCollider(gameObject, bubbleGeneratorParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.addComponent(new BubbleGeneratorMob(
                gameObject,
                bubbleGeneratorParameters.intValue(ParameterKey.HP),
                bubbleGeneratorParameters.floatValue(ParameterKey.ATTACK_INTERVAL),
                bubbleGeneratorParameters.floatValue(ParameterKey.ATTACK_RANGE),
                shootParameters.floatValue(ParameterKey.SPEED)
        ));
        gameObject.addComponent(new TimedSelfDestroyer(
                gameObject,
                bubbleGeneratorParameters.floatValue(ParameterKey.DURATION)
        ));
        gameObject.setElement(ElementType.WATER);
        gameObject.addComponent(new BuildingEffectReceiver(gameObject));
    }
}
