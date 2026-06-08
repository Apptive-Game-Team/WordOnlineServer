package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.EffectReceiver;
import com.wordonline.server.game.domain.object.component.physic.Collidable;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Status;

public class FrenzyTotem extends MagicComponent implements Collidable {
    private final float speed;
    private final float attackRange;
    private final float buffDuration;

    private boolean triggered = false;

    public FrenzyTotem(GameObject gameObject, float speed, float attackRange, float buffDuration) {
        super(gameObject);
        this.speed = speed;
        this.attackRange = attackRange;
        this.buffDuration = buffDuration;
    }

    @Override
    public void update() {
        gameObject.setPosition(gameObject.getPosition().plus(0, 0, -speed * getGameContext().getDeltaTime()));
        if (gameObject.getPosition().getZ() < 0) {
            trigger();
        }
    }

    @Override
    public void onCollision(GameObject otherObject) {
        trigger();
    }

    private void trigger() {
        if (triggered) return;
        triggered = true;

        gameObject.setStatus(Status.Attack);
        getGameContext().overlapSphereAll(gameObject, attackRange).stream()
                .map(target -> target.getComponent(EffectReceiver.class))
                .filter(effectReceiver -> effectReceiver != null)
                .forEach(effectReceiver -> effectReceiver.onReceive(Effect.Frenzy, buffDuration));
        gameObject.destroy();
    }
}
