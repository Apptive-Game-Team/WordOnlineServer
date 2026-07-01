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
    private final Effect visualEffect;

    public BaseStatusEffect(GameObject owner, float duration, StatusEffectKey key) {
        this(owner, duration, key, null);
    }

    public BaseStatusEffect(GameObject owner, float duration, StatusEffectKey key, Effect visualEffect) {
        super(owner);
        this.initialDuration = duration;
        this.remaining = duration;
        this.key = key;
        this.visualEffect = visualEffect;
        if (visualEffect != null) {
            gameObject.addEffect(visualEffect);
        }
    }

    public void resetDuration() {
        this.remaining = initialDuration;
    }

    @Override
    public void start() {
    }

    @Override
    public void update() {
        float dt = getGameContext().getDeltaTime();
        remaining -= dt;
        if (remaining <= 0) {
            expire();
        }
    }

    public abstract void onAttacked(ElementType attackType);

    protected void expire() {
        if (visualEffect != null) {
            gameObject.removeEffect(visualEffect);
        }
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
        if (visualEffect != null) {
            gameObject.removeEffect(visualEffect);
        }
    }
}
