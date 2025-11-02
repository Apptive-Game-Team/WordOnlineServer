package com.wordonline.server.game.dto.frame.projectile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PositionProjectileTarget extends ProjectileTarget {
    public final String targetType = "position";
    public float x, y, z;

}
