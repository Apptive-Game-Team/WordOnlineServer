package com.wordonline.server.game.domain.object.prefab.implement.subprefab;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.OnStartAttacker;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.magic.VineSpawnContext;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;

@Component("vine_prefab")
public class VinePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public VinePrefabInitializer(Parameters parameters) {
        super(PrefabType.Vine);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var vineParameters = parameters.object(GameObjectKey.VINE);
        gameObject.addCollider(new CircleCollider(gameObject, vineParameters.floatValue(ParameterKey.RADIUS), true));
        gameObject.setElement(ElementType.NATURE);
        gameObject.addComponent(new EffectProvider(gameObject, Effect.Snared));
        gameObject.addComponent(new TimedSelfDestroyer(gameObject, vineParameters.floatValue(ParameterKey.DURATION)));
        gameObject.addComponent(new OnStartAttacker(
                gameObject,
                vineParameters.floatValue(ParameterKey.RADIUS),
                vineParameters.intValue(ParameterKey.DAMAGE),
                VineSpawnContext.currentTracker()
        ));
    }
}
