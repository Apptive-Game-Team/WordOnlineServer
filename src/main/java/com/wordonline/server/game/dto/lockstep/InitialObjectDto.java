package com.wordonline.server.game.dto.lockstep;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Describes one object to spawn at session start (PVE only).
 * The client's SimWorld spawns the object deterministically; the server
 * tracks the installerId → object-id mapping for scenario event resolution.
 */
@Getter
@AllArgsConstructor
public class InitialObjectDto {
    private final String installerId;
    private final String prefabType;
    private final String master;
    private final double x;
    private final double y;
    private final double z;
}
