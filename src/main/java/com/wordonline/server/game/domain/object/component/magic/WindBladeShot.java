package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.effect.receiver.EffectReceiver;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WindBladeShot extends Shot {

    private final float radius;
    private final Set<Integer> piercedTargetIds = new HashSet<>();

    public WindBladeShot(GameObject gameObject, int damage, float speed, float radius) {
        super(gameObject, damage, speed);
        this.radius = radius;
    }

    @Override
    public void onCollision(GameObject otherObject) {
        if (otherObject == gameObject) {
            return;
        }

        if (piercedTargetIds.contains(otherObject.getId())) {
            return;
        }

        Master owner = gameObject.getMaster();
        if (owner != Master.None && otherObject.getMaster() == owner) {
            return;
        }

        List<Damageable> damageables = otherObject.getComponents(Damageable.class);
        if (damageables.isEmpty()) {
            return;
        }

        piercedTargetIds.add(otherObject.getId());

        gameObject.setStatus(Status.Attack);
        otherObject.setStatus(Status.Damaged);

        AttackInfo attackInfo = new AttackInfo(damage, gameObject.getElement().total());
        damageables.forEach(damageable -> damageable.onDamaged(attackInfo));

        EffectReceiver effectReceiver = otherObject.getComponent(EffectReceiver.class);
        Vector3 shotDirection = getDirection();
        if (effectReceiver != null && shotDirection != null) {
            float proximity = (float) (gameObject.getPosition().distance(otherObject.getPosition().toVector2()) / radius);
            effectReceiver.onReceive(Effect.Knockback, shotDirection, proximity);
        }
    }
}
