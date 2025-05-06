package com.wordonline.server.game.dto;

import com.wordonline.server.game.domain.Position;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
// This class is used to send updated object information to the client
public class UpdatedObjectDto {
    private final int id;
    private final Status status;
    private final Effect effect;
    private final Position position;
}
