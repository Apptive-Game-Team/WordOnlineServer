package com.wordonline.server.game.domain.object.prefab.implement.nature;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.VineToss;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("vine_toss_prefab")
public class VineTossPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public VineTossPrefabInitializer(Parameters parameters) {
        super(PrefabType.VineToss);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.setElement(ElementType.NATURE);
        gameObject.addComponent(new VineToss(gameObject));
    }
}
