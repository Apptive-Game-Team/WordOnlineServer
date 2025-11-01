package com.wordonline.server.game.dto.frame.projectile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReferenceProjectileTarget extends ProjectileTarget {
    public final String targetType = "reference";
    public int id;
}
