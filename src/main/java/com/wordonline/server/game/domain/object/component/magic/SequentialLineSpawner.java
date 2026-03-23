package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;

public class SequentialLineSpawner extends Component {

    private final Master master;
    private final PrefabType prefabType;
    private final Vector3 startPosition;
    private final Vector3 direction;
    private final float spacing;
    private final float spawnInterval;
    private int remainingSpawnCount;
    private int nextSpawnIndex;
    private float elapsed;

    public SequentialLineSpawner(GameObject gameObject,
                                 Master master,
                                 PrefabType prefabType,
                                 Vector3 startPosition,
                                 Vector3 direction,
                                 float spacing,
                                 float spawnInterval,
                                 int totalSpawnCount) {
        super(gameObject);
        this.master = master;
        this.prefabType = prefabType;
        this.startPosition = startPosition;
        this.direction = direction.normalize();
        this.spacing = spacing;
        this.spawnInterval = spawnInterval;
        this.remainingSpawnCount = Math.max(totalSpawnCount - 1, 0);
        this.nextSpawnIndex = 1;
        this.elapsed = 0f;
    }

    @Override
    public void start() {
    }

    @Override
    public void update() {
        if (remainingSpawnCount <= 0) {
            gameObject.removeComponent(this);
            return;
        }

        elapsed += getGameContext().getDeltaTime();
        while (elapsed >= spawnInterval && remainingSpawnCount > 0) {
            elapsed -= spawnInterval;
            Vector3 spawnPosition = startPosition.plus(direction.multiply(spacing * nextSpawnIndex));
            new GameObject(master, prefabType, spawnPosition, getGameContext());
            nextSpawnIndex++;
            remainingSpawnCount--;
        }

        if (remainingSpawnCount <= 0) {
            gameObject.removeComponent(this);
        }
    }

    @Override
    public void onDestroy() {
    }
}
