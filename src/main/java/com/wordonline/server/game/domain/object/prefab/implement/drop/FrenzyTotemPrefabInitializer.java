package com.wordonline.server.game.domain.object.prefab.implement.drop;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.FrenzyTotem;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("frenzy_totem_prefab")
public class FrenzyTotemPrefabInitializer extends PrefabInitializer {
    private final Parameters parameters;

    public FrenzyTotemPrefabInitializer(Parameters parameters) {
        super(PrefabType.FrenzyTotem);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var dropParameters = parameters.object(GameObjectKey.DROP);
        gameObject.addCollider(new CircleCollider(gameObject, dropParameters.floatValue(ParameterKey.RADIUS), true));
        gameObject.setElement(ElementType.NONE);
        gameObject.getComponents().add(new FrenzyTotem(gameObject));
    }
}
