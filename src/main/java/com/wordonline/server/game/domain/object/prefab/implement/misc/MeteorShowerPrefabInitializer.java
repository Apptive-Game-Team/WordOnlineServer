package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.RandomAreaSpawner;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component("meteor_shower_prefab")
public class MeteorShowerPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;
    private final Set<ElementType> elementSet = EnumSet.of(ElementType.FIRE, ElementType.ROCK);

    public MeteorShowerPrefabInitializer(Parameters parameters) {
        super(PrefabType.MeteorShower);
        this.parameters = parameters;
    }

    public void initialize(GameObject gameObject) {
        var meteorShowerParameters = parameters.object(GameObjectKey.METEOR_SHOWER);
        gameObject.setElement(elementSet);
        gameObject.getComponents().add(new RandomAreaSpawner(gameObject, PrefabType.MeteorDrop,
                meteorShowerParameters.intValue(ParameterKey.DURATION),
                meteorShowerParameters.floatValue(ParameterKey.ATTACK_INTERVAL),
                meteorShowerParameters.intValue(ParameterKey.RADIUS)));
    }
}

