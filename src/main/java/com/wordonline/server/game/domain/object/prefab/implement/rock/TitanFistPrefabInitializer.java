package com.wordonline.server.game.domain.object.prefab.implement.rock;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.TitanFistExplosion;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import org.springframework.stereotype.Component;

@Component("titan_fist_prefab")
public class TitanFistPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public TitanFistPrefabInitializer(Parameters parameters) {
        super(PrefabType.TitanFist);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var stats = parameters.object(GameObjectKey.TITAN_FIST);
        float radius = stats.floatValue(ParameterKey.RADIUS);
        gameObject.addCollider(new CircleCollider(gameObject, radius, true));
        gameObject.setElement(ElementType.ROCK);
        gameObject.addComponent(new TitanFistExplosion(
                gameObject,
                stats.intValue(ParameterKey.DAMAGE),
                radius
        ));
    }
}
