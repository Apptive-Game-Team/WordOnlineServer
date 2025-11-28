package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.domain.object.Vector3;

public interface Colliderable {

    Vector3 getPosition();
    boolean isCollidingWish(Colliderable collider);
}
