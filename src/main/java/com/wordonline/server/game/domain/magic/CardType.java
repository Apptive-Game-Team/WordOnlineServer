package com.wordonline.server.game.domain.magic;

import lombok.Getter;

@Getter
public enum CardType {
    // Card types
    Shoot(Type.Magic),
    Summon(Type.Magic),
    Spawn(Type.Magic),
    Explode(Type.Magic),

    Fire(Type.Type),
    Water(Type.Type),
    Lightning(Type.Type),
    Rock(Type.Type),
    Leaf(Type.Type);

    public enum Type {
        Magic,
        Type
    }
    private final Type type;

    CardType(Type type) {
        this.type = type;
    }
}