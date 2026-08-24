package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.physic.Collider;

import java.util.List;

public class CollisionChecker{
    // Indexed loops with an early return instead of nested anyMatch pipelines. anyMatch is
    // short-circuiting too, so the result is identical; what disappears is one stream
    // pipeline per collider pair. The wall is a single object carrying four EdgeColliders,
    // so every pair involving it used to build five pipelines.
    public static boolean isColliding(GameObject obj1, GameObject obj2) {
        List<Collider> colliders1 = obj1.getColliders();
        List<Collider> colliders2 = obj2.getColliders();

        for (int i = 0; i < colliders1.size(); i++) {
            Collider collider1 = colliders1.get(i);
            for (int j = 0; j < colliders2.size(); j++) {
                if (collider1.isCollidingWish(colliders2.get(j))) {
                    return true;
                }
            }
        }

        return false;
    }
}
