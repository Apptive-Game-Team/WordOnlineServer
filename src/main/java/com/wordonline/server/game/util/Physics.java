package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;

import java.util.List;

public interface Physics {
    List<GameObject> overlapSphereAll(GameObject object, float radius);
    GameObject raycast(GameObject object, Vector2 direction, float distance);
}
