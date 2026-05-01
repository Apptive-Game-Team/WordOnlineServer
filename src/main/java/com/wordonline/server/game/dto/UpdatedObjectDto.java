package com.wordonline.server.game.dto;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.util.MobHealthSelector;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
@AllArgsConstructor
// This class is used to send updated object information to the client
public class UpdatedObjectDto {
    private final int id;
    private int maxHp;
    private int hp;
    private Status status;
    private Effect effect;
    private Master master;
    private Vector3 position;

    public void updateHp(GameObject gameObject) {
        Mob mob = MobHealthSelector.findHealthMob(gameObject);
        if (mob != null) {
            this.maxHp = mob.getMaxHp();
            this.hp = mob.getHp();
        }
    }

    public UpdatedObjectDto(GameObject gameObject) {
        this.id = gameObject.getId();
        Mob mob = MobHealthSelector.findHealthMob(gameObject);
        if (mob != null) {
            this.maxHp = mob.getMaxHp();
            this.hp = mob.getHp();
        } else {
            this.maxHp = -1;
            this.hp = -1;
        }

        this.status = gameObject.getStatus();
        this.effect = gameObject.getEffect();
        this.master = gameObject.getMaster();
        this.position = gameObject.getPosition();
    }
}
