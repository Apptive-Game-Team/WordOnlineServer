package com.wordonline.server.game.domain.object.prefab.implement.drop;

import com.wordonline.server.game.domain.Parameters;
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
        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("rallying_torch", "radius"), true));
        gameObject.setElement(EnumSet.of(ElementType.FIRE, ElementType.WIND));
        gameObject.addComponent(new RallyingTorch(
                gameObject,
                (float) parameters.getValue("rallying_torch", "duration")));
    }
}
