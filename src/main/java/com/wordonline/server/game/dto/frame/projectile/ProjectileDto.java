package com.wordonline.server.game.dto.frame.projectile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectileDto {
    public String type;
    public ProjectileTarget start;
    public ProjectileTarget end;
    public float duration;
}
