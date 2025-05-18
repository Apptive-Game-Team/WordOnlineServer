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
        int deltaX = (int) (endPosition.getX() - startPosition.getX());
        int deltaY = (int) (endPosition.getY() - startPosition.getY());
        int steps = Math.max(Math.abs(deltaX), Math.abs(deltaY));

        for (int i = 0; i <= steps; i++) {
            float x = startPosition.getX() + (deltaX * i) / (float) steps;
            float y = startPosition.getY() + (deltaY * i) / (float) steps;
            path.add(new Vector2(x, y));
        }

        return path;
    }
}
