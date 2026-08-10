package com.wordonline.server.game.domain.object.component.mob.detector;

import com.wordonline.server.game.domain.object.GameObject;

import java.util.function.Predicate;

public interface Detector {
    public static final int DETECTING_INTERVAL = 1;
    GameObject detect(GameObject self);

    GameObject detect(GameObject self, Predicate<GameObject> filter);
}
