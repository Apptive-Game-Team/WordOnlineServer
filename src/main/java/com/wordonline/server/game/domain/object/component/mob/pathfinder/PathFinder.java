package com.wordonline.server.game.domain.object.component.mob.pathfinder;

import com.wordonline.server.game.domain.object.Vector3;

import java.util.List;

public interface PathFinder {
    public static final float REACH_THRESHOLD  = 0.5f;
    List<Vector3> findPath(Vector3 startPosition, Vector3 endPosition);
}
