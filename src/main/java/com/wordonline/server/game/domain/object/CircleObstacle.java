package com.wordonline.server.game.domain.object;

public class CircleObstacle {
    public final Vector2 center;
    public final float radius;
    public CircleObstacle(Vector2 center, float radius) {
        this.center = center;
        this.radius = radius;
    }
}