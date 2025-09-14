package com.wordonline.server.game.dto.frame;

public record SnapshotResponseDto(int frame, java.util.List<SnapshotObjectDto> objects) {}