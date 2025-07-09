package com.wordonline.server.game.dto;

import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
@AllArgsConstructor
// This class is used to send updated object information to the client
public class UpdatedObjectDto {
    private final int id;
    private final int maxHp;
    private final int hp;
    private Status status;
    private Effect effect;
    private Vector2 position;

    public UpdatedObjectDto(GameObject gameObject) {
        this.id = gameObject.getId();
        Mob mob = gameObject.getComponent(Mob.class);
        if (mob != null) {
            this.maxHp = mob.getMaxHp();
            this.hp = mob.getHp();
        } else {
            this.maxHp = -1;
            this.hp = -1;
        }

        this.status = gameObject.getStatus();
        this.effect = gameObject.getEffect();
        this.position = gameObject.getPosition();
    }
}
