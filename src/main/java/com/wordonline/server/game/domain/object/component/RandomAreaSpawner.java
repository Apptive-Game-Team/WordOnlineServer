package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

import java.util.concurrent.ThreadLocalRandom;

public class RandomAreaSpawner extends TimedSelfDestroyer {

    private final float spawnInterval;
    private float elapsed;
    private final float areaRadius;
    private final PrefabType prefabType;
    public RandomAreaSpawner(GameObject gameObject, PrefabType spawnPrefabType, float timeToLive, float spawnInterval, float areaRadius) {
        super(gameObject, timeToLive);
        this.spawnInterval = spawnInterval;
        this.areaRadius = areaRadius;
        this.prefabType = spawnPrefabType;
    }


    @Override
    public void update() {
        super.update();
        elapsed += getGameContext().getDeltaTime();
        if (elapsed >= spawnInterval) {
            elapsed -= spawnInterval;
            Vector3 pos = getRandomVectorInCircle(areaRadius).toVector3(GameConfig.DROP_MAGIC_INITIAL_HEIGHT) ;
            new GameObject(gameObject.getMaster(), prefabType,
                    pos, getGameContext());
        }
    }

    private Vector2 getRandomVectorInCircle(float radius) {
        Vector3 center = gameObject.getPosition(); // x,y from your Vector3
        double angle = ThreadLocalRandom.current().nextDouble(0.0, Math.PI);
        double dist  = Math.sqrt(ThreadLocalRandom.current().nextDouble()) * radius; // uniform in disk
        float x = (float) (center.getX() + dist * Math.cos(angle));
        float y = (float) (center.getY() + dist * Math.sin(angle));
        return new Vector2(x, y);
    }

}
