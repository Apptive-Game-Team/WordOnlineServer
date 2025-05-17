package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;

public interface CollisionChecker {
    boolean isColliding(GameObject obj1, GameObject obj2);
}