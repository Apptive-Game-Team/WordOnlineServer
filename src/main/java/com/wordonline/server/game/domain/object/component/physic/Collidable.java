package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.domain.object.GameObject;

public interface Collidable {
    void onCollision(GameObject otherObject);
}