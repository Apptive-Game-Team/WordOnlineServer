package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;

import java.util.List;

public interface Physics {

    default List<GameObject> overlapSphereAll(GameObject object, float radius) {
        return overlapSphereAll(object.getPosition(), radius);
    }

    List<GameObject> overlapSphereAll(Vector3 position, float radius);
    GameObject raycast(GameObject object, Vector2 direction, float distance);
}
