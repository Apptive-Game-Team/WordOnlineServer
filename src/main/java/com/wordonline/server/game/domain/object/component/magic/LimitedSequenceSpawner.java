package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

public class LimitedSequenceSpawner extends Component {
    private static final float SPAWN_OFFSET_STEP = 0.35f;

    private final float spawnIntervalSec;
    private final int maxSpawnCount;
    private final PrefabType[] prefabTypes;
    private float elapsed;
    private int spawnedCount;

    public LimitedSequenceSpawner(GameObject gameObject,
                                  float spawnIntervalSec,
                                  int maxSpawnCount,
                                  PrefabType... prefabTypes) {
        super(gameObject);
        this.spawnIntervalSec = Math.max(0.1f, spawnIntervalSec);
        this.maxSpawnCount = maxSpawnCount;
        this.prefabTypes = prefabTypes.clone();
    }

    @Override
    public void start() {
    }

    @Override
    public void update() {
        if ((maxSpawnCount > 0 && spawnedCount >= maxSpawnCount) || prefabTypes.length == 0) {
            return;
        }

        elapsed += getGameContext().getDeltaTime();
        if (elapsed < spawnIntervalSec) {
            return;
        }

        elapsed = 0f;
        PrefabType prefabType = prefabTypes[spawnedCount % prefabTypes.length];
        Vector3 spawnPosition = nextSpawnPosition();
        new GameObject(gameObject.getMaster(), prefabType, spawnPosition, getGameContext());
        spawnedCount++;
    }

    @Override
    public void onDestroy() {
    }

    private Vector3 nextSpawnPosition() {
        int slot = spawnedCount % 6;
        float offsetX = ((slot % 3) - 1) * SPAWN_OFFSET_STEP;
        float offsetY = ((slot / 3) - 0.5f) * SPAWN_OFFSET_STEP;
        return gameObject.getPosition().plus(offsetX, offsetY, 0);
    }
}
