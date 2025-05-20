package com.wordonline.server.game.domain.object;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
// This class is used to represent the position of an object in the game
public class Vector2 {
    private float x, y;

    public Vector2 add(float x, float y) {
        return new Vector2(this.x + x, this.y + y);
    }

    public Vector2 add(Vector2 vector) {
        return new Vector2(this.x + vector.x, this.y + vector.y);
    }

    public Vector2 subtract(float x, float y) {
        return new Vector2(this.x - x, this.y - y);
    }

    public Vector2 subtract(Vector2 vector) {
        return new Vector2(this.x - vector.x, this.y - vector.y);
    }

    public Vector2 multiply(float scalar) {
        return new Vector2(this.x * scalar, this.y * scalar);
    }

    public Vector2 scale(float scalar) {
        return new Vector2(x * scalar, y * scalar);
    }

    public double dot(Vector2 other) {
        return this.x * other.x + this.y * other.y;
    }
    
    public Vector2 normalize() {
        double length = Math.sqrt(x * x + y * y);
        if (length == 0) {
            return new Vector2(0, 0);
        }
        return new Vector2((float) (x / length), (float) (y / length));
    }

    public double distance(Vector2 other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }
}
