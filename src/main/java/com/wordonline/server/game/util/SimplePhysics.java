package com.wordonline.server.game.util;

import java.util.ArrayList;
import java.util.List;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;

public class SimplePhysics implements Physics {
    private final List<GameObject> gameObjects;

    public SimplePhysics(List<GameObject> gameObjects) {
        this.gameObjects = gameObjects;
    }

    @Override
    public List<GameObject> overlapSphereAll(GameObject origin, float radius) {
        List<GameObject> result = new ArrayList<>();
        CircleCollider circle = new CircleCollider(origin, radius, true);

        for (GameObject other : gameObjects) {
            if (other == origin) continue;
            if(circle.isCollidingWish(other.getColliders().getFirst())) result.add(other);
        }
        return result;
    }

    @Override
    public GameObject raycast(GameObject object, Vector2 direction, float distance) {
        Vector2 origin = object.getPosition().toVector2();
        Vector2 dirNorm = direction.normalize();

        GameObject closest = null;
        double closestDist = distance + 1;

        for (GameObject other : gameObjects) {
            if (other == object) continue;

            Vector2 toOther = other.getPosition().toVector2().subtract(origin);
            double projection = toOther.dot(dirNorm);

            if (projection < 0 || projection > distance) continue;

            if (CollisionChecker.isColliding(object, other) && projection < closestDist) {
                closest = other;
                closestDist = projection;
            }
        }

        return closest;
    }
}
    
