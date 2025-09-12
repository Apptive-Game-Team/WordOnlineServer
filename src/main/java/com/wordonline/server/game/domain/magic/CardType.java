package com.wordonline.server.game.domain.magic;

import com.wordonline.server.game.config.GameConfig;
import lombok.Getter;

@Getter
public enum CardType {
    Dummy(Type.Magic, 10, GameConfig.WIDTH),

    // Card types
    Shoot(Type.Magic, 15, GameConfig.WIDTH),
    @Deprecated
    Drop(Type.Magic, 25, GameConfig.WIDTH * 3f / 4),
    Summon(Type.Magic, 30, GameConfig.WIDTH / 3f),
    Spawn(Type.Magic, 20, GameConfig.WIDTH / 3f),
    Explode(Type.Magic, 10, GameConfig.WIDTH / 2f),

    Fire(Type.Type, 10, GameConfig.WIDTH / 4f),
    Water(Type.Type, 10, GameConfig.WIDTH / 4f),
    Lightning(Type.Type, 10, GameConfig.WIDTH / 4f),
    Rock(Type.Type, 10, GameConfig.WIDTH / 4f),
    Leaf(Type.Type, 10, GameConfig.WIDTH / 4f),
    Wind(Type.Type, 10, GameConfig.WIDTH / 4f);

    public enum Type {
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