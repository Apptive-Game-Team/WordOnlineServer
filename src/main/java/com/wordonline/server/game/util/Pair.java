package com.wordonline.server.game.util;

public record Pair<T>(T a, T b) {
    public Pair(T a, T b) {
        if (a.hashCode() < b.hashCode()) {
            this.a = a;
            this.b = b;
        } else {
            this.a = b;
            this.b = a;
        }
    }
}