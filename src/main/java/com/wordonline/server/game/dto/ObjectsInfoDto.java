package com.wordonline.server.game.dto;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ObjectsInfoDto {
    private final List<CreatedObjectDto> create;
    private final List<UpdatedObjectDto> update;
}


