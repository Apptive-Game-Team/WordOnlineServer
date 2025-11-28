package com.wordonline.server.game.domain.object.component.mob.simple;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;

import lombok.Setter;

public abstract class TimedBehaviorMob extends Mob {

    private final float interval;

    @Setter
    private Behavior behavior;

    private float timer = 0;

    public TimedBehaviorMob(GameObject gameObject, int maxHp,
            float speed, float interval, Behavior behavior) {
        super(gameObject, maxHp, speed);
        this.interval = interval;
        this.behavior = behavior;
    }

    @Override
    public void update() {
        super.update();
        timer += getGameContext().getDeltaTime();
        if (timer >= interval) {
            if (behavior.behave()) {
                timer = 0;
            }
        }
    }
}
