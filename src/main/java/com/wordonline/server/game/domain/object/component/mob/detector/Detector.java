package com.wordonline.server.game.domain.object.component.mob.detector;

import com.wordonline.server.game.domain.object.GameObject;

public interface Detector {
    public static final int DETECTING_INTERVAL = 1;
    GameObject detect(GameObject self);
}
