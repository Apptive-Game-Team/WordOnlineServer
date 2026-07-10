package com.wordonline.server.game.domain.object.prefab.implement.explode;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.ShockOverloadExplosion;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("shock_overload_prefab")
public class ShockOverloadPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public ShockOverloadPrefabInitializer(Parameters parameters) {
        super(PrefabType.ShockOverload);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var explodeParameters = parameters.object(GameObjectKey.SHOCK_OVERLOAD);
        gameObject.addCollider(new CircleCollider(
                gameObject,
                explodeParameters.floatValue(ParameterKey.RADIUS),
                true
        ));
        gameObject.setElement(ElementType.LIGHTNING);
        gameObject.addComponent(new ShockOverloadExplosion(
                gameObject,
                explodeParameters.intValue(ParameterKey.DAMAGE),
                explodeParameters.floatValue(ParameterKey.RADIUS)
        ));
    }
}
