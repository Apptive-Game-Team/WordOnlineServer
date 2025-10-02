package com.wordonline.server.game.dto.frame;

import com.wordonline.server.game.domain.magic.CardType;

import java.util.List;

public record SnapshotResponseDto(
        int frame,
        List<SnapshotObjectDto> objects,
        List<CardType> myCards
) {}