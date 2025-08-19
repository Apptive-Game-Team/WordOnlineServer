package com.wordonline.server.game.dto.frame;

public record SnapshotObjectDto(
        int id,
        String prefab,
        float x, float y,
        String status,
        String effect
) {}