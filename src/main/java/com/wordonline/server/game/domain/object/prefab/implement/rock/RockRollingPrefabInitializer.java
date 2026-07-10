package com.wordonline.server.game.domain.object.prefab.implement.rock;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.RollingRock;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("rock_rolling_prefab")
public class RockRollingPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public RockRollingPrefabInitializer(Parameters parameters) {
        super(PrefabType.RockRolling);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var shootParameters = parameters.object(GameObjectKey.ROCK_ROLLING);
        float radius = shootParameters.floatValue(ParameterKey.RADIUS);
        gameObject.addCollider(new CircleCollider(gameObject, radius, true));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new RollingRock(gameObject,
                shootParameters.intValue(ParameterKey.DAMAGE),
                shootParameters.floatValue(ParameterKey.SPEED),
                radius
        ));
    }
}
