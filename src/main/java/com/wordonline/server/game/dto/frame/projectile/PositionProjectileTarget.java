package com.wordonline.server.game.dto.frame.projectile;

import lombok.Data;

@Data
public class PositionProjectileTarget extends ProjectileTarget {
    public final String targetType = "position";
    public float x, y, z;
}
