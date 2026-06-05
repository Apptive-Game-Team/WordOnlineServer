package com.wordonline.server.game.domain.object.prefab.implement.drop;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.RallyingTorch;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("rallying_torch_prefab")
public class RallyingTorchPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public RallyingTorchPrefabInitializer(Parameters parameters) {
        super(PrefabType.RallyingTorch);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var rallyingTorchParameters = parameters.object(GameObjectKey.RALLYING_TORCH);
        gameObject.addCollider(new CircleCollider(gameObject, rallyingTorchParameters.floatValue(ParameterKey.RADIUS), true));
        gameObject.setElement(EnumSet.of(ElementType.FIRE, ElementType.WIND));
        gameObject.addComponent(new RallyingTorch(
                gameObject,
                rallyingTorchParameters.floatValue(ParameterKey.DURATION)));
    }
}
