package com.wordonline.server.game.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class ObjectsInfoDto {
    private final List<CreatedObjectDto> create;
    private final List<UpdatedObjectDto> update;
}


