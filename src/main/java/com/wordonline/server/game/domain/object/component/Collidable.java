package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.object.GameObject;

public interface Collidable {
    void onCollision(GameObject otherObject);
}