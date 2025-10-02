package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.dto.Effect;
import lombok.Getter;


public abstract class BaseStatusEffect extends Component {
    protected float initialDuration;
    protected float remaining;
    @Getter
    protected final StatusEffectKey key;

    public BaseStatusEffect(GameObject owner, float duration, StatusEffectKey key) {
        super(owner);
        this.initialDuration = duration;
        this.remaining = duration;
        this.key = key;
    }

    public void resetDuration() {
        this.remaining = initialDuration;
    }

    @Override
    public void update() {
        float dt = gameObject.getGameLoop().deltaTime;
        remaining -= dt;
        if (remaining <= 0) {
            expire();
        }
    }

    public abstract void onAttacked(ElementType attackType);

    protected void expire() {
        gameObject.setEffect(Effect.None);
        gameObject.removeComponent(this);
    }

    public void refresh(float duration)
    {
        this.initialDuration = duration;
        this.remaining = duration;
        start();
    }

    public void extend(float duration)
    {
        this.remaining += duration;
    }

    @Override
    public void onDestroy() {
    }
}