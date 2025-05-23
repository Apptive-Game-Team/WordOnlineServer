package com.wordonline.server.game.domain.magic;

import com.wordonline.server.game.config.GameConfig;
import lombok.Getter;

@Getter
public enum CardType {
    Dummy(Type.Magic, 10, GameConfig.WIDTH),

    // Card types
    Shoot(Type.Magic, 25, GameConfig.WIDTH),
    @Deprecated
    Drop(Type.Magic, 35, GameConfig.WIDTH * 3f / 4),
    Summon(Type.Magic, 15, GameConfig.WIDTH / 3f),
    Explode(Type.Magic, 40, GameConfig.WIDTH / 2f),

    Fire(Type.Type, 0, GameConfig.WIDTH / 4f),
    Ice(Type.Type, 0, GameConfig.WIDTH / 4f),
    Lightning(Type.Type, 0, GameConfig.WIDTH / 4f),
    Rock(Type.Type, 0, GameConfig.WIDTH / 4f),
    Leaf(Type.Type, 0, GameConfig.WIDTH / 4f);

    enum Type {
        Magic,
        Type
    }
    private final Type type;
    private final float range;
    private final int manaCost;
    CardType(Type type, int manaCost, float range) {
        this.type = type;
        this.manaCost = manaCost;
        this.range = range;
    }
}