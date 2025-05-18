package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.object.GameObject;

public interface Collidable {
    public void onCollision(GameObject otherObject);
}