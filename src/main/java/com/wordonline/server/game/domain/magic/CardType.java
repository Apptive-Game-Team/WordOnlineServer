package com.wordonline.server.game.domain.magic;

import lombok.Getter;

@Getter
public enum CardType {
    Shoot(Type.Magic),
    Build(Type.Magic),
    Spawn(Type.Magic),
    Explode(Type.Magic),
    Drop(Type.Magic),

    Fire(Type.Type),
    Water(Type.Type),
    Lightning(Type.Type),
    Rock(Type.Type),
    Nature(Type.Type),
    Wind(Type.Type);

    public enum Type {
        Magic,
        Type
    }
    private final Type type;

    CardType(Type type) {
        this.type = type;
    }
}