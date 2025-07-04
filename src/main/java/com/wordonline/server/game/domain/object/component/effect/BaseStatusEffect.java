package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.dto.Effect;

public abstract class BaseStatusEffect extends Component {
    private final float initialDuration;
    private float remaining;

    public BaseStatusEffect(GameObject owner, float duration) {
        super(owner);
        this.initialDuration = duration;
        this.remaining = duration;
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

    //특정 속성 공격 맞았을떄
    public abstract void handleAttack(ElementType attackType);

    protected void expire() {
        gameObject.setEffect(Effect.None);
        gameObject.getComponentsToRemove().add(this);
    }

    @Override
    public void onDestroy() {
    }
}