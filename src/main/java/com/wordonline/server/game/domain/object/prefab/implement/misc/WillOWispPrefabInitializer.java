package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.MindControlShot;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("will_o_wisp_prefab")
public class WillOWispPrefabInitializer extends PrefabInitializer {
    private final Parameters parameters;

    public WillOWispPrefabInitializer(Parameters parameters) {
        super(PrefabType.WillOWisp);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var shootParameters = parameters.object(GameObjectKey.WILL_O_WISP);
        gameObject.addCollider(new CircleCollider(gameObject, shootParameters.floatValue(ParameterKey.RADIUS), true));
        gameObject.setElement(ElementType.NONE);
        gameObject.getComponents().add(new MindControlShot(gameObject, shootParameters.floatValue(ParameterKey.SPEED)));
    }
}
