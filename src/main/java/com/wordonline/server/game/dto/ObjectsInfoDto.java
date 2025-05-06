package com.wordonline.server.game.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
// This class is used to send object information to the client
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


