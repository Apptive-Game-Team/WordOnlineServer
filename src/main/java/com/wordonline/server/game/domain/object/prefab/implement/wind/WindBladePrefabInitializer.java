package com.wordonline.server.game.domain.object.prefab.implement.wind;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.WindBladeShot;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("wind_blade_prefab")
public class WindBladePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public WindBladePrefabInitializer(Parameters parameters) {
        super(PrefabType.WindBlade);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var shootParameters = parameters.object(GameObjectKey.WIND_BLADE);
        float radius = shootParameters.floatValue(ParameterKey.RADIUS);
        gameObject.addCollider(new CircleCollider(gameObject, radius, true));
        gameObject.setElement(ElementType.WIND);
        gameObject.getComponents().add(new WindBladeShot(gameObject,
                shootParameters.intValue(ParameterKey.DAMAGE),
                shootParameters.floatValue(ParameterKey.SPEED),
                radius
        ));
    }
}
