package com.wordonline.server.game.domain.object.prefab.implement.subprefab;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.Drop;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("meteor_drop_prefab")
public class MeteorDropPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public MeteorDropPrefabInitializer(Parameters parameters) {
        super(PrefabType.MeteorDrop);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var meteorDropParameters = parameters.object(GameObjectKey.METEOR_DROP);
        gameObject.addCollider(new CircleCollider(gameObject, meteorDropParameters.floatValue(ParameterKey.RADIUS), true));
        gameObject.setElement(EnumSet.of(ElementType.FIRE, ElementType.ROCK));
        gameObject.getComponents().add(new Drop(gameObject, meteorDropParameters.intValue(ParameterKey.DAMAGE)));
    }
}

