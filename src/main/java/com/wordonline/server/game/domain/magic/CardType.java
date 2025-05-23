package com.wordonline.server.game.domain.magic;

import lombok.Getter;

@Getter
public enum CardType {
    Dummy(Type.Magic, 10),

    // Card types
    Shoot(Type.Magic, 25),
    Drop(Type.Magic, 35),
    Summon(Type.Magic, 15),
    Explode(Type.Magic, 40),
    @Deprecated
    Drop(Type.Magic, 35, GameConfig.WIDTH * 3f / 4),

    Fire(Type.Type, 0),
    Ice(Type.Type, 0),
    Lightning(Type.Type, 0),
    Rock(Type.Type, 0),
    Leaf(Type.Type, 0);

    enum Type {
        Magic,
        Type
    }
    private final Type type;
    private final int manaCost;
    CardType(Type type, int manaCost) {
        this.type = type;
        this.manaCost = manaCost;
    }
}