package com.wordonline.server.game.dto.frame;

public record SnapshotObjectDto(
        int id,
        String prefab,
        float x, float y, float z,
        String master,
        String status,
        String effect
) {}