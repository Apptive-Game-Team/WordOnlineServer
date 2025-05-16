package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;
import java.util.List;

public interface Raycast {
    public List<GameObject> circle(GameObject object, float distance);
}
