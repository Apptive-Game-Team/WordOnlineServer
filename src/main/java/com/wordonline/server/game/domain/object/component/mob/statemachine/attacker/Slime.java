package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.dto.Master;

public class Slime extends MeleeAttackMob {

    public Slime(GameObject gameObject, int maxHp, float speed, int targetMask, int damage, float attackInterval) {
        super(gameObject, maxHp, speed, targetMask, damage, attackInterval);
    }

    @Override
    public void onDeath() {
        if(gameObject.getElement().nativeHas(ElementType.FIRE)) {
            new GameObject(gameObject, Master.None, PrefabType.FireField);
        } else if (gameObject.getElement().nativeHas(ElementType.WATER)) {
            new GameObject(gameObject, Master.None, PrefabType.WaterField);
        } else if (gameObject.getElement().nativeHas(ElementType.LIGHTNING)) {
            new GameObject(gameObject, Master.None, PrefabType.ElectricField);
        } else if (gameObject.getElement().nativeHas(ElementType.NATURE)) {
            new GameObject(gameObject, Master.None, PrefabType.LeafField);
        }
        super.onDeath();
    }
}
