package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Collidable;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class BruteCollisionSystem implements CollisionSystem {

    private final CollisionChecker collisionChecker = new CollisionChecker();

    @Override
    public void checkAndHandleCollisions(List<GameObject> gameObjects) {
        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject a = gameObjects.get(i);
            List<Collidable> collidableAList = a.getComponents(Collidable.class);
            if (collidableAList.isEmpty()) continue;

            for (int j = i + 1; j < gameObjects.size(); j++) {
                GameObject b = gameObjects.get(j);
                List<Collidable> collidableBList = b.getComponents(Collidable.class);
                if (collidableBList.isEmpty()) continue;

                if (collisionChecker.isColliding(a, b)) {
                    log.info("Collision detected between {} and {}", a.getType(), b.getType());
                    collidableAList.forEach(collidable -> collidable.onCollision(b));
                    collidableBList.forEach(collidable -> collidable.onCollision(a));
                }
            }
        }
    }
}
