package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Master;

public class CollisionChecker{
    public static boolean isColliding(GameObject obj1, GameObject obj2) {
        if (obj1.getMaster() == obj2.getMaster() &&
                obj1.getMaster() != Master.None) {
            return false;
        }

        return obj1.getColliders().stream()
            .anyMatch(a -> obj2.getColliders().stream()
                    .anyMatch(a::isCollidingWish));
    }
}
