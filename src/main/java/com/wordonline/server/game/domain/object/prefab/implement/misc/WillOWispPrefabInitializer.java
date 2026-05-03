package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
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
        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("shoot", "radius"), true));
        gameObject.setElement(ElementType.NONE);
        gameObject.getComponents().add(new MindControlShot(gameObject, (float) parameters.getValue("shoot", "speed")));
    }
}
