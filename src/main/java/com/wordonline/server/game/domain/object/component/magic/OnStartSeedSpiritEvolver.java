package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

public class OnStartSeedSpiritEvolver extends Component {

    private final float radius;
    private final PrefabType evolvedPrefabType;

    public OnStartSeedSpiritEvolver(
            GameObject gameObject,
            float radius,
            PrefabType evolvedPrefabType
    ) {
        super(gameObject);
        this.radius = radius;
        this.evolvedPrefabType = evolvedPrefabType;
    }

    @Override
    public void start() {
        SeedSpiritEvolver.evolveAlliedSeedSpirits(
                gameObject,
                radius,
                evolvedPrefabType
        );
    }

    @Override
    public void update() {
    }

    @Override
    public void onDestroy() {
    }
}
