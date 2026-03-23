package com.wordonline.server.game.domain.object.prefab.implement.nature;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.VineShot;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("leaf_shot_prefab")
public class LeafShotPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public LeafShotPrefabInitializer(Parameters parameters) {
        super(PrefabType.LeafShot);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.setElement(ElementType.NATURE);
        gameObject.getComponents().add(new VineShot(gameObject));
    }
}
