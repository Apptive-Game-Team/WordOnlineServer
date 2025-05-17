package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;

import java.util.List;
import java.util.Vector;

public interface Physics {
    List<GameObject> overlapCircleAll(GameObject object, float distance);
    GameObject raycast(GameObject object, Vector2 direction, float distance);
}
