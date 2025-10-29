package com.wordonline.server.game.dto.frame;

import com.wordonline.server.game.dto.UpdatedObjectDto;
import com.wordonline.server.game.dto.frame.projectile.ProjectileDto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
// This class is used to send object information to the client
public class ObjectsInfoDto {
    private final List<CreatedObjectDto> create;
    private final List<UpdatedObjectDto> update;
    private final List<ProjectileDto> projectile;

    public ObjectsInfoDto(){
        this(
                List.of(), List.of(), List.of()
        );
    }

    public ObjectsInfoDto(List<CreatedObjectDto> createdObjectDtos, List<UpdatedObjectDto> updatedObjectDtos, List<ProjectileDto> projectileDtos) {
        create = createdObjectDtos;
        update = updatedObjectDtos;
        projectile = projectileDtos;
    }
}


