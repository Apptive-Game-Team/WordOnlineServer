package com.wordonline.server.game.domain.object.component.mob.pathfinder;

import com.wordonline.server.game.domain.object.Vector3;

import java.util.ArrayList;
import java.util.List;

public class SimplePathFinder implements PathFinder {
    @Override
    public List<Vector3> findPath(Vector3 startPosition, Vector3 endPosition) {
        List<Vector3> path = new ArrayList<>();
        float deltaX = endPosition.getX() - startPosition.getX();
        float deltaZ = endPosition.getZ() - startPosition.getZ();
        int steps = Math.max(Math.abs((int) deltaX), Math.abs((int) deltaZ));

        if (steps == 0) {
            path.add(endPosition.grounded());
            return path;
        }

        for (int i = 0; i <= steps; i++) {
            float x = startPosition.getX() + (deltaX * i) / steps;
            float z = startPosition.getZ() + (deltaZ * i) / steps;
            path.add(new Vector3(x, 0, z));
        }

        return path;
    }
}
