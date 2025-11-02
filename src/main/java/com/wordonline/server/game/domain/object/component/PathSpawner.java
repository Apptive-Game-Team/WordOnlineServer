package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.dto.Master;

public class PathSpawner extends Component {

    private final PrefabType prefabType;
    private final float interval;
    private Vector2 lastSpawnPosition;

    public PathSpawner(GameObject gameObject, PrefabType prefabType, float interval) {
        super(gameObject);
        this.prefabType = prefabType;
        this.interval = interval;
        this.lastSpawnPosition = gameObject.getPosition().toVector2();
    }

    @Override
    public void start() { }

    @Override
    public void update() {
        double distance = gameObject.getPosition().distance(lastSpawnPosition);
        if (distance >= interval) {
            lastSpawnPosition = gameObject.getPosition().toVector2();
            new GameObject(gameObject, Master.None, prefabType);
        }
    }

    @Override
    public void onDestroy() { }
}
