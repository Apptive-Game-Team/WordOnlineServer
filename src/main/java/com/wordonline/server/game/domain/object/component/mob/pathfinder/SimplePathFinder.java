package com.wordonline.server.game.domain.object.component.mob.pathfinder;

import com.wordonline.server.game.domain.object.Vector2;

import java.util.ArrayList;
import java.util.List;

public class SimplePathFinder implements PathFinder {
    @Override
    public List<Vector2> findPath(Vector2 startPosition, Vector2 endPosition) {
        // Implement a simple pathfinding algorithm here
        // For example, a straight line path
        List<Vector2> path = new ArrayList<>();
        float deltaX = endPosition.getX() - startPosition.getX();
        float deltaY = endPosition.getY() - startPosition.getY();
        int steps = Math.max(Math.abs((int) deltaX), Math.abs((int) deltaY));

        for (int i = 0; i <= steps; i++) {
            float x = startPosition.getX() + (deltaX * i) / steps;
            float y = startPosition.getY() + (deltaY * i) / steps;
            path.add(new Vector2(x, y));
        }

        return path;
    }
}
