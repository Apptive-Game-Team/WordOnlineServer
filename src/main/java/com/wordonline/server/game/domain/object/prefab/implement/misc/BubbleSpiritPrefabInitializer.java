package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.BubbleSpiritMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("bubble_spirit_prefab")
public class BubbleSpiritPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public BubbleSpiritPrefabInitializer(Parameters parameters) {
        super(PrefabType.BubbleSpirit);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var bubbleSpiritParameters = parameters.object(GameObjectKey.BUBBLE_SPIRIT);
        var shootParameters = parameters.object(GameObjectKey.SHOOT);
        gameObject.addComponent(new RigidBody(gameObject, bubbleSpiritParameters.intValue(ParameterKey.MASS)));
        gameObject.addComponent(new ZPhysics(gameObject, GameConfig.AERIAL_MOB_INIT_HEIGHT));
        gameObject.addCollider(new CircleCollider(gameObject, bubbleSpiritParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.addComponent(new BubbleSpiritMob(gameObject,
                bubbleSpiritParameters.intValue(ParameterKey.HP),
                bubbleSpiritParameters.floatValue(ParameterKey.SPEED),
                bubbleSpiritParameters.intValue(ParameterKey.DAMAGE),
                bubbleSpiritParameters.floatValue(ParameterKey.ATTACK_INTERVAL),
                bubbleSpiritParameters.floatValue(ParameterKey.ATTACK_RANGE),
                shootParameters.floatValue(ParameterKey.SPEED)
        ));
        gameObject.setElement(EnumSet.of(ElementType.WATER, ElementType.WIND));
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
