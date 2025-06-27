package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;

import java.util.List;

public interface CollisionSystem {
    void checkAndHandleCollisions(List<GameObject> gameObjects);
}
