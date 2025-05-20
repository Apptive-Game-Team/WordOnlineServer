package com.wordonline.server.game.util;

import java.util.ArrayList;
import java.util.List;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;

public class SimplePhysics implements Physics{
    private final List<GameObject> gameObjects;

    public SimplePhysics(List<GameObject> gameObjects) {
        this.gameObjects = gameObjects;
    }


    @Override
    public List<GameObject> overlapCircleAll(GameObject object, float distance) {
        List<GameObject> result = new ArrayList<>();
        Vector2 origin = object.getPosition();

        for (GameObject other : gameObjects) {
            if (other == object) continue;

            double dist = origin.distance(other.getPosition());
            if (dist <= distance + other.getRadius()) {
                result.add(other);
            }
        }

        return result;
    }

    @Override
    public GameObject raycast(GameObject object, Vector2 direction, float distance) {
        Vector2 origin = object.getPosition();
        Vector2 dirNorm = direction.normalize();

        GameObject closest = null;
        double closestDist = distance + 1;

        for (GameObject other : gameObjects) {
            if (other == object) continue;

            Vector2 toOther = other.getPosition().subtract(origin);
            double projection = toOther.dot(dirNorm);

            if (projection < 0 || projection > distance) continue;

            Vector2 closestPoint = origin.add(dirNorm.scale(((float)projection)));
            double perpendicularDist = other.getPosition().distance(closestPoint);

            if (perpendicularDist <= other.getRadius() && projection < closestDist) {
                closest = other;
                closestDist = projection;
            }
        }

        return closest;
    }
}
    
