package com.wordonline.server.game.dto.frame.projectile;

import com.wordonline.server.game.domain.object.Vector3;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PositionProjectileTarget extends ProjectileTarget {
    public final String targetType = "position";
    public float x, y, z;

    public PositionProjectileTarget(Vector3 position) {
        this(position.getX(), position.getY(), position.getZ());
    }
}
