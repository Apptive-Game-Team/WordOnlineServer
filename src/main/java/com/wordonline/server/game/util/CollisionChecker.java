package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;

public class CollisionChecker{
    public static boolean isColliding(GameObject obj1, GameObject obj2) {
        return obj1.getColliders().stream()
            .anyMatch(a -> obj2.getColliders().stream()
                    .anyMatch(a::isCollidingWish));
    }
}
