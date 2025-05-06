package com.wordonline.server.game.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class ObjectsInfoDto {
    private final List<CreatedObjectDto> create;
    private final List<UpdatedObjectDto> update;

    public ObjectsInfoDto(){
        create = new ArrayList<>();
        update = new ArrayList<>();
    }

    public ObjectsInfoDto(List<CreatedObjectDto> createdObjectDtos, List<UpdatedObjectDto> updatedObjectDtos) {
        create = createdObjectDtos;
        update = updatedObjectDtos;
    }
}


