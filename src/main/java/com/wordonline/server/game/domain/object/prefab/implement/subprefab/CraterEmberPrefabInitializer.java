package com.wordonline.server.game.domain.object.prefab.implement.subprefab;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.CraterEmber;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("crater_ember_prefab")
public class CraterEmberPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public CraterEmberPrefabInitializer(Parameters parameters) {
        super(PrefabType.CraterEmber);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var craterEmberParameters = parameters.object(GameObjectKey.CRATER_EMBER);
        gameObject.addCollider(new CircleCollider(gameObject, craterEmberParameters.floatValue(ParameterKey.RADIUS), true));
        gameObject.setElement(ElementType.FIRE);
        gameObject.addComponent(new CraterEmber(
                gameObject,
                craterEmberParameters.intValue(ParameterKey.DAMAGE),
                craterEmberParameters.floatValue(ParameterKey.ATTACK_RANGE)
        ));
    }
}
