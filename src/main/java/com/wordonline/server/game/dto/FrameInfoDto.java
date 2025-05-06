package com.wordonline.server.game.dto;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.core.ReactiveAdapterRegistry;

import java.util.List;

@Setter
@RequiredArgsConstructor
public class FrameInfoDto {
    private final String type = "frame";
    private final int updatedMana;
    private final CardInfoDto cards;
    private final ObjectsInfoDto objects;
}


