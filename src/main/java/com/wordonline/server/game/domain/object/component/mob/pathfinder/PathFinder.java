package com.wordonline.server.game.domain.object.component.mob.pathfinder;

import com.wordonline.server.game.domain.object.Vector2;

import java.util.List;

public interface PathFinder {
    public static final int REACH_THRESHOLD  = 2;
    List<Vector2> findPath(Vector2 startPosition, Vector2 endPosition);
}
