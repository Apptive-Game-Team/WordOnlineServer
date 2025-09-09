package com.wordonline.server.game.domain.object;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
// This class is used to represent the position of an object in the game
public class Vector3 {
    private float x, y, z;

    public static final Vector3 ZERO = new Vector3(0, 0, 0);

    public void clear() {
        x = 0; y = 0;
    }

    public void add(Vector3 vector3) {
        x += vector3.x;
        y += vector3.y;
        z += vector3.z;
    }

    public Vector3 plus(float x, float y, float z) {
        return new Vector3(this.x + x, this.y + y, this.z + z);
    }

    public Vector3 plus(Vector2 vector) {
        return plus(vector.getX(), vector.getY(), 0);
    }

    public Vector3 plus(Vector3 vector) {
        return plus(vector.x, vector.y, vector.z);
    }

    public Vector3 subtract(float x, float y, float z) {
        return new Vector3(this.x - x, this.y - y, this.z - z);
    }

    public Vector3 subtract(Vector3 vector) {
        return subtract(vector.x, vector.y, vector.z);
    }

    public Vector3 multiply(float scalar) {
        return new Vector3(this.x * scalar, this.y * scalar, this.x * scalar);
    }

    public float dot(Vector3 other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }
    
    public Vector3 normalize() {
        double length = Math.sqrt(x * x + y * y + z * z);
        if (length == 0) {
            return new Vector3(0, 0, 0);
        }
        return new Vector3((float) (x / length), (float) (y / length), (float) (z / length));
    }

    public double distance(Vector3 other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2) + Math.pow(this.z - other.z, 2));
    }

    // Vector3의 차원을 낮춰서 적용
    public double distance(Vector2 other) {
        return Math.sqrt(Math.pow(this.x - other.getX(), 2) + Math.pow(this.y - other.getY(), 2));
    }

    public Vector2 toVector2() {
        return new Vector2(this);
    }
}
