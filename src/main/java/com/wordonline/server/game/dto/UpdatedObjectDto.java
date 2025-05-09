package com.wordonline.server.game.dto;

import com.wordonline.server.game.domain.object.Position;
import com.wordonline.server.game.domain.object.GameObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
@AllArgsConstructor
// This class is used to send updated object information to the client
public class UpdatedObjectDto {
    private final int id;
    private Status status;
    private Effect effect;
    private Position position;

    public UpdatedObjectDto(GameObject gameObject) {
        this.id = gameObject.getId();
        this.status = gameObject.getStatus();
        this.effect = gameObject.getEffect();
        this.position = gameObject.getPosition();
    }
}
