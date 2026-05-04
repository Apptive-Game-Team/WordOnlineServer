package com.wordonline.server.game.dto.frame;

import com.wordonline.server.game.dto.UpdatedObjectDto;
import com.wordonline.server.game.dto.frame.projectile.ProjectileDto;

import lombok.AllArgsConstructor;

import java.util.List;

// This class is used to send object information to the client
public record ObjectsInfoDto(
        List<CreatedObjectDto> create,
        List<UpdatedObjectDto> update,
        List<ProjectileDto> projectile
) {

    public ObjectsInfoDto() {
        this(
                List.of(), List.of(), List.of()
        );
    }
}


