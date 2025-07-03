package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Master;

public class CollisionChecker{
    public boolean isColliding(GameObject obj1, GameObject obj2) {
        if (obj1.getMaster() == obj2.getMaster() ||
                obj1.getMaster() != Master.None) {
            return false;
        }
        double dist = obj1.getPosition().distance(obj2.getPosition());
        return  dist <= (obj1.getRadius() + obj2.getRadius());
    }
}
