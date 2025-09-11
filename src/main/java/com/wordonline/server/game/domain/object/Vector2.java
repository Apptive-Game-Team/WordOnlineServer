package com.wordonline.server.game.domain.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
// This class is used to represent the position of an object in the game
public class Vector2 {
    private float x, y;

    public static final Vector2 ZERO = new Vector2(0, 0);

    public void clear() {
        x = 0; y = 0;
    }

    public void add(Vector2 vector2) {
        x += vector2.getX();
        y += vector2.getY();
    }

    public Vector2 plus(float x, float y) {
        return new Vector2(this.x + x, this.y + y);
    }

    public Vector2 plus(Vector2 vector) {
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

    public float dot(Vector2 other) {
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

    public Vector2(Vector3 vector3) {
        this(vector3.getX(), vector3.getY());
    }

    public Vector3 toVector3() {
        return new Vector3(x, y, 0);
    }
}
