package com.wordonline.server.game.dto.frame;

public record SnapshotObjectDto(
        int id,
        String prefab,
        float x, float y,
        float radius,
        String status,
        String effect
) {}