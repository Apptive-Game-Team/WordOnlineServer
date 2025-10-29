package com.wordonline.server.game.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MutablePair<A, B> {
    private A first;
    private B second;

    public MutablePair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public static  <A, B> MutablePair of(A first, B second) {
        return new MutablePair(first, second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
